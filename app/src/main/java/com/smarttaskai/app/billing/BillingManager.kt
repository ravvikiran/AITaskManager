package com.smarttaskai.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.smarttaskai.app.data.preferences.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) {

    companion object {
        const val PRODUCT_PREMIUM_MONTHLY = "premium_monthly"
        const val PRODUCT_PREMIUM_YEARLY = "premium_yearly"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 3

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = BillingState.Cancelled
            }
            else -> {
                _billingState.value = BillingState.Error(
                    "Purchase failed: ${billingResult.debugMessage}"
                )
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        connectToPlayBilling()
    }

    private fun connectToPlayBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    reconnectAttempts = 0
                    _billingState.value = BillingState.Connected
                    // Check existing purchases
                    queryExistingPurchases()
                } else {
                    _billingState.value = BillingState.Error("Billing setup failed")
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = BillingState.Disconnected
                // Retry connection with limit to prevent infinite loop
                if (reconnectAttempts < maxReconnectAttempts) {
                    reconnectAttempts++
                    connectToPlayBilling()
                }
            }
        })
    }

    /**
     * Launch the purchase flow for a subscription product.
     */
    fun launchPurchaseFlow(activity: Activity, productId: String) {
        scope.launch {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                    val productDetails = productDetailsList.first()
                    val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@queryProductDetailsAsync

                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    billingClient.launchBillingFlow(activity, billingFlowParams)
                } else {
                    _billingState.value = BillingState.Error("Product not found")
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge the purchase
            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgeParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        scope.launch {
                            userPreferences.setPremium(true)
                            _billingState.value = BillingState.PurchaseComplete
                        }
                    }
                }
            } else {
                scope.launch {
                    userPreferences.setPremium(true)
                    _billingState.value = BillingState.PurchaseComplete
                }
            }
        }
    }

    private fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val activePurchases = purchasesList.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                scope.launch {
                    if (activePurchases.isNotEmpty()) {
                        userPreferences.setPremium(true)
                    } else {
                        userPreferences.setPremium(false)
                    }
                }
            }
        }
    }
}

sealed class BillingState {
    data object Idle : BillingState()
    data object Connected : BillingState()
    data object Disconnected : BillingState()
    data object PurchaseComplete : BillingState()
    data object Cancelled : BillingState()
    data class Error(val message: String) : BillingState()
}
