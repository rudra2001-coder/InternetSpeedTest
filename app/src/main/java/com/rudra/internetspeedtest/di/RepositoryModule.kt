package com.rudra.internetspeedtest.di

import com.rudra.internetspeedtest.data.repository.CdnRepositoryImpl
import com.rudra.internetspeedtest.data.repository.SpeedTestRepositoryImpl
import com.rudra.internetspeedtest.data.repository.TestHistoryRepositoryImpl
import com.rudra.internetspeedtest.domain.repository.CdnRepository
import com.rudra.internetspeedtest.domain.repository.SpeedTestRepository
import com.rudra.internetspeedtest.domain.repository.TestHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCdnRepository(impl: CdnRepositoryImpl): CdnRepository

    @Binds
    @Singleton
    abstract fun bindSpeedTestRepository(impl: SpeedTestRepositoryImpl): SpeedTestRepository

    @Binds
    @Singleton
    abstract fun bindTestHistoryRepository(impl: TestHistoryRepositoryImpl): TestHistoryRepository
}