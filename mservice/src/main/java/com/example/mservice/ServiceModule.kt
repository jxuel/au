package com.example.mservice

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.annotation.OptIn
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "last")

@Module
@InstallIn(ServiceComponent::class)
class ServiceModule {
    @Provides
    fun provideContext(app: Application): Context = app.applicationContext

    @Provides
    fun provideResources(app: Application): Resources = app.resources

    @OptIn(UnstableApi::class)
    @Provides
    fun provideDataProvider(app: Application): DatabaseProvider =
        StandaloneDatabaseProvider(app.applicationContext)
    @OptIn(UnstableApi::class)
    @Provides
    @ServiceScoped
    @Named("ds")
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return appContext.dataStore
    }

    @OptIn(UnstableApi::class)
    @Provides
    @ServiceScoped
    @Named("playerCache")
    fun providePlayerCache(
        app: Application,
        databaseProvider: DatabaseProvider
    ): SimpleCache = SimpleCache(
        app.filesDir.resolve("exoplayer"),
        when (val cacheSize = 1024) {
            -1 -> NoOpCacheEvictor()
            else -> LeastRecentlyUsedCacheEvictor(cacheSize * 1024 * 1024L)
        },
        databaseProvider
    )

    @OptIn(UnstableApi::class)
    @Provides
    @ServiceScoped
    @Named("downloadCache")
    fun provideDownloadCache(
        app: Application,
        databaseProvider: DatabaseProvider
    ): SimpleCache = SimpleCache(
        app.filesDir.resolve("download"),
        when (val cacheSize = 1024) {
            -1 -> NoOpCacheEvictor()
            else -> LeastRecentlyUsedCacheEvictor(cacheSize * 1024 * 1024L)
        },
        databaseProvider
    )

    @OptIn(UnstableApi::class)
    @Provides
    @Named("uriCache")
    fun provideUriCache() = ConcurrentHashMap<String, MediaURICache>()
}