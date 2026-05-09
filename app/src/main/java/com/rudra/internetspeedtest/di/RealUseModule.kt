package com.rudra.internetspeedtest.di

import com.rudra.internetspeedtest.feature.realuse.FileDownloadPredictor
import com.rudra.internetspeedtest.feature.realuse.GamingLatencySimulator
import com.rudra.internetspeedtest.feature.realuse.HouseholdSimulator
import com.rudra.internetspeedtest.feature.realuse.NetflixStreamingSimulator
import com.rudra.internetspeedtest.feature.realuse.SocialMediaSimulator
import com.rudra.internetspeedtest.feature.realuse.StreamingReadinessChecker
import com.rudra.internetspeedtest.feature.realuse.VideoCallSimulator
import com.rudra.internetspeedtest.feature.realuse.YouTubeStreamingSimulator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealUseModule {

    @Provides @Singleton
    fun provideYouTubeStreamingSimulator(): YouTubeStreamingSimulator = YouTubeStreamingSimulator()

    @Provides @Singleton
    fun provideNetflixStreamingSimulator(): NetflixStreamingSimulator = NetflixStreamingSimulator()

    @Provides @Singleton
    fun provideVideoCallSimulator(): VideoCallSimulator = VideoCallSimulator()

    @Provides @Singleton
    fun provideGamingLatencySimulator(): GamingLatencySimulator = GamingLatencySimulator()

    @Provides @Singleton
    fun provideSocialMediaSimulator(): SocialMediaSimulator = SocialMediaSimulator()

    @Provides @Singleton
    fun provideFileDownloadPredictor(): FileDownloadPredictor = FileDownloadPredictor()

    @Provides @Singleton
    fun provideHouseholdSimulator(): HouseholdSimulator = HouseholdSimulator()

    @Provides @Singleton
    fun provideStreamingReadinessChecker(): StreamingReadinessChecker = StreamingReadinessChecker()
}
