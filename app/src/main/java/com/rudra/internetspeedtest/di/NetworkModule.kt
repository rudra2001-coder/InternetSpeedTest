package com.rudra.internetspeedtest.di

import com.rudra.internetspeedtest.core.network.ConnectionDetector
import com.rudra.internetspeedtest.core.testing.AdaptiveThreadController
import com.rudra.internetspeedtest.core.testing.ConfidenceCalculator
import com.rudra.internetspeedtest.core.testing.ServerPoolManager
import com.rudra.internetspeedtest.core.testing.TestAuditor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideServerPoolManager(): ServerPoolManager = ServerPoolManager()

    @Provides @Singleton
    fun provideAdaptiveThreadController(): AdaptiveThreadController = AdaptiveThreadController()

    @Provides @Singleton
    fun provideConfidenceCalculator(): ConfidenceCalculator = ConfidenceCalculator()

    @Provides @Singleton
    fun provideTestAuditor(): TestAuditor = TestAuditor()
}
