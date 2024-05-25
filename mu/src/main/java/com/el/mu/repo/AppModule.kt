package com.el.mu.repo

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.el.mu.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideSqlDriver(app: Application): SqlDriver {
        return AndroidSqliteDriver(
            schema = Database.Schema,
            context = app,
            name = "test.db"
        )
    }

    @Provides
    @Singleton
    fun provideMediaEntityRepo(driver: SqlDriver): MediaEntityRepo {
        return MediaEntityRepoImpl(Database(driver))
    }

    @Provides
    @Singleton
    fun provideMediaTrackRepo(driver: SqlDriver): MediaTrackRepo {
        return MediaTrackRepoImpl(Database(driver))
    }

    @Provides
    @Singleton
    fun provideSearchHistoryRepo(driver: SqlDriver): SearchHistoryRepo {
        return SearchHistoryRepoImpl(Database(driver))
    }

    @Provides
    @Singleton
    fun provideTrackEntityRepo(driver: SqlDriver): TrackEntityRepo {
        return TrackEntityRepoImpl(Database(driver))
    }

    @Provides
    @Singleton
    fun provideArtworkRepo(driver: SqlDriver): ArtworkRepo {
        return ArtworkRepoImpl(Database(driver))
    }
}