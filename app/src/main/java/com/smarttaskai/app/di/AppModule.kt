package com.smarttaskai.app.di

import com.smarttaskai.app.data.local.dao.ProductivityLogDao
import com.smarttaskai.app.ml.ProductivityMLService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideProductivityMLService(
        productivityLogDao: ProductivityLogDao
    ): ProductivityMLService {
        return ProductivityMLService(productivityLogDao)
    }
}
