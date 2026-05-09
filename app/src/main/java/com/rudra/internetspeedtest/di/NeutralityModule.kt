package com.rudra.internetspeedtest.di

import com.rudra.internetspeedtest.feature.neutrality.CautiousLanguageEngine
import com.rudra.internetspeedtest.feature.neutrality.ISPComparisonEngine
import com.rudra.internetspeedtest.feature.neutrality.NeutralityReportExporter
import com.rudra.internetspeedtest.feature.neutrality.NeutralityScoreEngine
import com.rudra.internetspeedtest.feature.neutrality.ServiceSpeedTester
import com.rudra.internetspeedtest.feature.neutrality.TemporalConsistencyChecker
import com.rudra.internetspeedtest.feature.neutrality.ZeroRatingDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NeutralityModule {

    @Provides @Singleton
    fun provideServiceSpeedTester(): ServiceSpeedTester = ServiceSpeedTester()

    @Provides @Singleton
    fun provideNeutralityScoreEngine(): NeutralityScoreEngine = NeutralityScoreEngine()

    @Provides @Singleton
    fun provideCautiousLanguageEngine(): CautiousLanguageEngine = CautiousLanguageEngine()

    @Provides @Singleton
    fun provideZeroRatingDetector(): ZeroRatingDetector = ZeroRatingDetector()

    @Provides @Singleton
    fun provideTemporalConsistencyChecker(): TemporalConsistencyChecker = TemporalConsistencyChecker()

    @Provides @Singleton
    fun provideISPComparisonEngine(): ISPComparisonEngine = ISPComparisonEngine()

    @Provides @Singleton
    fun provideNeutralityReportExporter(): NeutralityReportExporter = NeutralityReportExporter()
}
