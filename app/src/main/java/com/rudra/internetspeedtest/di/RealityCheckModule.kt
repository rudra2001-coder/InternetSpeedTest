package com.rudra.internetspeedtest.di

import android.content.Context
import com.rudra.internetspeedtest.core.testing.ConfidenceCalculator
import com.rudra.internetspeedtest.domain.repository.InternetSpeedTestRepository
import com.rudra.internetspeedtest.feature.realitycheck.RealityCheckEngine
import com.rudra.internetspeedtest.feature.realitycheck.RealityCheckHistory
import com.rudra.internetspeedtest.feature.realitycheck.RealityCheckReport
import com.rudra.internetspeedtest.feature.realitycheck.RealityCheckScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealityCheckModule {
    
    @Provides
    @Singleton
    fun provideRealityCheckEngine(
        speedTestRepository: InternetSpeedTestRepository,
        confidenceCalculator: ConfidenceCalculator
    ): RealityCheckEngine {
        return RealityCheckEngine(
            speedTestEngine = speedTestRepository,
            confidenceCalculator = confidenceCalculator
        )
    }
    
    @Provides
    @Singleton
    fun provideRealityCheckHistory(
        @ApplicationContext context: Context
    ): RealityCheckHistory {
        return RealityCheckHistory(context)
    }
    
    @Provides
    @Singleton
    fun provideRealityCheckReport(
        @ApplicationContext context: Context
    ): RealityCheckReport {
        return RealityCheckReport(context)
    }
    
    @Provides
    @Singleton
    fun provideRealityCheckScheduler(
        @ApplicationContext context: Context
    ): RealityCheckScheduler {
        return RealityCheckScheduler(context)
    }
}