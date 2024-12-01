package com.example.mservice

import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_MAX_BUFFER_MS
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.el.ytrepo.YTClient
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Named


@UnstableApi
@AndroidEntryPoint
@ServiceScoped
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    @Inject
    @Named("playerCache")
    lateinit var playerCache: SimpleCache

    @Inject
    @Named("downloadCache")
    lateinit var downloadCache: SimpleCache

    @Inject
    lateinit var databaseProvider: DatabaseProvider

    @Inject
    @Named("uriCache")
    lateinit var uriCache: ConcurrentHashMap<String, MediaURICache>

    @Inject
    @Named("ds")
    lateinit var dataStore: DataStore<Preferences>
    val lMediaId = stringPreferencesKey("media_id")
    val lTitle = stringPreferencesKey("title")
    val lArtUri = stringPreferencesKey("art_uri")
    val lArtist = stringPreferencesKey("artist")
    companion object {
        const val CHUNK_LENGTH = 512 * 1024L
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val player =
            ExoPlayer.Builder(this).setWakeMode(C.WAKE_MODE_NETWORK).setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(5000).setLoadControl(
                    DefaultLoadControl.Builder()
                        //.setAllocator(DefaultAllocator(true, 16))
                        .setBufferDurationsMs(
                            5_500,
                            DEFAULT_MAX_BUFFER_MS,
                            DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                            DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                        ).build()
                ).setMediaSourceFactory(
                    DefaultMediaSourceFactory(createDataSourceFactory())
                ).build()

        mediaSession =
            MediaSession.Builder(this, player).setCallback(object : MediaSession.Callback {
                override fun onPlaybackResumption(
                    mediaSession: MediaSession, controller: MediaSession.ControllerInfo
                ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                    val settable =
                        SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
                    CoroutineScope(Dispatchers.IO).launch {
                        // Your app is responsible for storing the playlist and the start position
                        // to use here
                        settable.set(MediaSession.MediaItemsWithStartPosition(listOf(loadLast()), 0, 0))
                    }
                    return settable
                }
            }).build()
    }

    private suspend fun loadLast(): MediaItem {
        val lastMedia = dataStore.data.first()
        return MediaItem.Builder()
            .setMediaId(lastMedia[lMediaId] ?: "")
            .setUri("youtube://music/${lastMedia[lMediaId]}")
            //.setMimeType("audio/opus")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(lastMedia[lTitle])
                    .setArtworkUri(Uri.parse(lastMedia[lArtUri] ?: ""))
                    .setArtist(lastMedia[lArtist])
                    .build()
            ).build()
    }

    private suspend fun saveLast(item: MediaItem) {
        dataStore.edit { lastMedia ->
            lastMedia[lMediaId] = item.mediaId
            item.mediaMetadata.title?.let {t->
                lastMedia[lTitle] = t.toString()
            }
            item.mediaMetadata.artworkUri?.let {u->
                lastMedia[lArtUri] = u.toString()
            }
            item.mediaMetadata.artist?.let {a->
                lastMedia[lArtist] = a.toString()
            }
        }
    }
    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        mediaSession?.run {
            player.currentMediaItem?.let { item->
                runBlocking(Dispatchers.IO) {
                    saveLast(item)
                }
            }
            player.release()
            release()
            mediaSession = null
        }
        playerCache.release()
        downloadCache.release()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        mediaSession?.player?.let {
            if (it.playWhenReady) {
                it.pause()
            }
        }
        stopSelf()
    }


    @OptIn(UnstableApi::class)
    private fun createCacheDataSource(): CacheDataSource.Factory =
        CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(
                CacheDataSource.Factory()
                    .setCache(playerCache)
                    .setUpstreamDataSourceFactory(
                        DefaultDataSource.Factory(
                            this, OkHttpDataSource.Factory(
                                OkHttpClient.Builder().build()
                            )
                        )
                    )
            ).setCacheWriteDataSinkFactory(null).setFlags(FLAG_IGNORE_CACHE_ON_ERROR)

    private fun createDataSourceFactory(): DataSource.Factory {
        val RESOLVABLE_SCHEME = "youtube"
        val client = YTClient()
        suspend fun resolveUri(uri: Uri): Uri {
            uri.lastPathSegment?.let { videoId ->
                var cached = uriCache[videoId]
                if (cached == null || uriCache[videoId]!!.createdAt + uriCache[videoId]!!.expireIn < System.currentTimeMillis()) {
                    val url = client.getMediaById(videoId).body()?.streamingData?.last()?.url ?: ""
                    if (url.isNotEmpty()) {
                        cached = MediaURICache(url, 3600000, System.currentTimeMillis())
                        uriCache[videoId] = cached
                    }
                }
                cached
            }?.let { mediaLink ->
                return Uri.parse(mediaLink.uri)
            }
            return Uri.EMPTY
        }
        return ResolvingDataSource.Factory(createCacheDataSource()) { dataSpecIn ->
            var dataSpecOut = dataSpecIn
            if (RESOLVABLE_SCHEME == dataSpecIn.uri.scheme) {
                runBlocking {
                    val resolvedUrl: Uri = resolveUri(dataSpecIn.uri)
                    dataSpecOut = dataSpecOut.withUri(resolvedUrl)
                        .subrange(dataSpecOut.uriPositionOffset, CHUNK_LENGTH)
                }
            }
            return@Factory dataSpecOut

        }
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession
}
