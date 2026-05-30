# TensorFlow Lite
-keep class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Google Play Billing
-keep class com.android.vending.billing.** { *; }

# Keep data classes used with Room
-keep class com.smarttaskai.app.data.local.entity.** { *; }
-keep class com.smarttaskai.app.data.local.dao.ProductiveHourResult { *; }
