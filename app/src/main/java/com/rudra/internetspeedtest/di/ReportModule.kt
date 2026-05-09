package com.rudra.internetspeedtest.di

import com.rudra.internetspeedtest.report.AnomalyDetector
import com.rudra.internetspeedtest.report.ConnectionHealthScoreCalculator
import com.rudra.internetspeedtest.report.ISPTransparencyReport
import com.rudra.internetspeedtest.report.PeerComparison
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReportModule {

    @Provides @Singleton
    fun provideConnectionHealthScoreCalculator(): ConnectionHealthScoreCalculator = ConnectionHealthScoreCalculator()

    @Provides @Singleton
    fun provideAnomalyDetector(): AnomalyDetector = AnomalyDetector()

    @Provides @Singleton
    fun providePeerComparison(): PeerComparison = PeerComparison()

    @Provides @Singleton
    fun provideISPTransparencyReport(): ISPTransparencyReport = ISPTransparencyReport()
}
