package com.rudra.internetspeedtest.di

import android.content.Context
import androidx.room.Room
import com.rudra.internetspeedtest.data.local.CdnDatabase
import com.rudra.internetspeedtest.data.local.dao.TestResultDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CdnDatabase {
        return Room.databaseBuilder(
            context,
            CdnDatabase::class.java,
            CdnDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideTestResultDao(database: CdnDatabase): TestResultDao {
        return database.testResultDao()
    }
}