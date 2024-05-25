package com.el.mu

import app.cash.sqldelight.db.SqlDriver
import com.el.mu.repo.ArtworkRepo
import com.el.mu.repo.MediaEntityRepo
import com.el.mu.repo.MediaTrackRepo
import com.el.mu.repo.TrackEntityRepo
import com.el.mu.service.MediaDBService
import com.el.mu.service.MediaDBServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    fun provideMediaDBService(
        mediaEntityRepo: MediaEntityRepo,
        trackEntityRepo: TrackEntityRepo,
        artworkRepo: ArtworkRepo,
        mediaTrackRepo: MediaTrackRepo,
        driver: SqlDriver
    ): MediaDBService {
        return MediaDBServiceImpl(
            mediaEntityRepo,
            trackEntityRepo,
            artworkRepo,
            mediaTrackRepo,
            driver
        )
    }
}