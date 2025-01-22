package com.el.mu

import com.el.mu.ui.local.LocalAudio
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.session.MediaController
import com.el.mu.data.ActionResult
import com.el.mu.repo.MediaEntityRepo
import com.el.mu.service.MediaDBService
import com.el.ytrepo.YTClient
import com.el.ytrepo.data.MediaInfo
import com.el.ytrepo.data.PlayListRequestBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val mediaEntityRepo: MediaEntityRepo,
    private val mediaDBService: MediaDBService
) : ViewModel() {
    companion object {
        private val dImage = Uri.parse("android.resource://com.el.mu/" + R.raw.dds)
        private val lImage = Uri.parse("android.resource://com.el.mu/" + R.raw.music_file)
        private val dTitle = "Nothing is playing now"
        private val TAG = PlayerViewModel::class.java.name
    }


    private val client = YTClient()

    lateinit var mctl: MediaController
        get

    private val _playList by lazy {
        MutableStateFlow(getPlaylist())
    }

    //private var _playList = MutableStateFlow<List<String>>(emptyList())
    var playList: StateFlow<List<String>> = _playList

    private var _duration = MutableStateFlow<Long>(0)
    var duration: StateFlow<Long> = _duration

    private var _curTitle = MutableStateFlow<String>(dTitle)
    var currentTitle: StateFlow<String> = _curTitle

    private var _curImage = MutableStateFlow<Uri>(dImage)
    var currentImage: StateFlow<Uri> = _curImage

    private var _isPlaying = MutableStateFlow<Boolean>(false)
    var isPlaying: StateFlow<Boolean> = _isPlaying
    private var _repeatMode = MutableStateFlow<Int>(Player.REPEAT_MODE_OFF)
    var repeatMode: StateFlow<Int> = _repeatMode


    fun initializedMctl(): Boolean {
        return ::mctl.isInitialized
    }

    fun getPlaylist(): List<String> {
        if (this::mctl.isInitialized) {
            if (mctl.isConnected) {
                return MutableList(mctl.mediaItemCount) { idx ->
                    mctl.getMediaItemAt(idx).mediaMetadata.title.toString()
                }
            }
        }
        return emptyList()
    }

    fun togglePlayer() {
        if (isPlaying.value) {
            mctl.pause()
        } else {
            mctl.play()
        }

    }
    fun toggleRepeatMode() {
        when (repeatMode.value) {
            Player.REPEAT_MODE_OFF -> mctl.repeatMode = Player.REPEAT_MODE_ONE // Set to repeat one track
            Player.REPEAT_MODE_ONE -> mctl.repeatMode = Player.REPEAT_MODE_ALL // Set to repeat all tracks
            Player.REPEAT_MODE_ALL -> mctl.repeatMode = Player.REPEAT_MODE_OFF // Set to no repeat
        }
        _repeatMode.value = mctl.repeatMode
    }
    fun setMediaController(mediaController: MediaController) {
        mctl = mediaController

        _isPlaying.value = mctl.isPlaying
        mctl.currentMediaItem?.mediaMetadata?.let {
            _curTitle.value = (it.title ?: dTitle).toString()
            _curImage.value = it.artworkUri ?: dImage
        }
        _repeatMode.value = mctl.repeatMode

        mctl.addListener(object : Player.Listener {
            private val retryLimit = 3
            private val retryCntMap = ConcurrentHashMap<String, Int>();

            private var playbackStartAt = 0L
            private var playbackDuration = 0L
            private var mediaDuration = 0L
            private var counted = false

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                // The player has transitioned to a new media item.
                // Handle this event as needed.
                val mediaMetadata: MediaMetadata? = mediaItem?.mediaMetadata
                mediaMetadata?.let {
                    _curTitle.value = (it.title ?: dTitle).toString()
                    _curImage.value = it.artworkUri ?: dImage
                    //Log.d("FDS",_curTitle.value + " " + _curImage.value)
                }
                counted = false
                //Log.d(TAG, "Changing $this")
                //val title: String? = mediaMetadata?.title
                //println("New media item playing: $title")
            }


            override fun onTimelineChanged(
                timeline: Timeline,
                @Player.TimelineChangeReason reason: Int
            ) {
                if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
                    // Update the UI according to the modified playlist (add, move or remove).
                    updateUiForPlaylist(timeline)
                }
            }

            fun updateUiForPlaylist(timeline: Timeline) {
                val tw = Timeline.Window()
                if (_playList.value.isEmpty()) {
                    _playList.value = MutableList<String>(timeline.windowCount) { idx ->
                        timeline.getWindow(idx, tw).mediaItem.mediaMetadata.title.toString()
                    }
                } else {
                    val res = MutableList<String>(timeline.windowCount) { idx ->
                        timeline.getWindow(idx, tw).mediaItem.mediaMetadata.title.toString()
                    }.toList()
                    if (res != _playList.value) {
                        _playList.value = res
                        if (res.isEmpty()) {
                            _curImage.value = dImage
                            _curTitle.value = dTitle
                        }
                        //println("DDF Initial")
                    } else {
                        //println("DDF Same")
                    }
                }
                /*for (i in 0 until timeline.windowCount) {
                    val a = timeline.getWindow(i, tw)
                    println("DDDA ${a.durationMs}")
                }
                val p = Timeline.Period()
                for (i in 0 until timeline.periodCount) {
                    val a = timeline.getPeriod(i, p)
                    println("DDDA ${a.id}")

                }

                println("DDDF ${timeline.isEmpty} ${timeline.windowCount} ${timeline.periodCount} ")
                for (i in 0 until mctl.mediaItemCount) {
                    val m = mctl.getMediaItemAt(i)
                    val info = m.mediaMetadata
                    println("DDDF $currentWindowIndex ${info.title} ${info.artist} ${m.localConfiguration?.uri}")
                }
                println("DDD $ ${mctl.getMediaItemAt(currentWindowIndex).mediaMetadata.title}")*/
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                _isPlaying.value = isPlaying
                if (isPlaying && !counted) {
                    //println("RRR ${mctl.mediaMetadata.title}")
                    mediaEntityRepo.insert(mctl.mediaMetadata.title.toString())
                    counted = true
                }
            }

            private suspend fun resetMediaUri(mediaItem: MediaItem): MediaItem {
                val uri = getMediaById(mediaItem.mediaId)
                Log.d(TAG, uri)
                return mediaItem
                    .buildUpon()
                    .setUri(uri)
                    .build()
                //.setUri("https://rr1---sn-p5qlsndr.googlevideo.com/videoplayback?expire=1703738064&ei=cKaMZZjeJoSJ_9EP8sWy-Aw&ip=73.212.187.42&id=o-ABBicWs7AqolOsr6RY51F1hkfcZpWci4FmtuWFSE0QGc&itag=251&source=youtube&requiressl=yes&xpc=EgVo2aDSNQ%3D%3D&mh=L8&mm=31%2C29&mn=sn-p5qlsndr%2Csn-p5qddn7d&ms=au%2Crdu&mv=m&mvi=1&pl=15&gcr=us&initcwndbps=1281250&vprv=1&mime=audio%2Fwebm&gir=yes&clen=2763593&dur=157.581&lmt=1603797422528340&mt=1703716149&fvip=4&keepalive=yes&fexp=24007246&c=ANDROID_MUSIC&txp=5531432&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cxpc%2Cgcr%2Cvprv%2Cmime%2Cgir%2Cclen%2Cdur%2Clmt&sig=AJfQdSswRQIgR3ZUyjyB7kJdnd0-2k5nF_pxkdvlpu7QXVky1tjWyzoCIQD0s-x7zo1guvvJm8tQVrl9RbTSjxwxrSCQVNhr-78paQ%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AAO5W4owRgIhAJc2aRW_Xq60-FXh2ImxfyJ57BKYjbz0mVnRaOIxmXaQAiEA6gLFYInIeio4yo8QAZ3riVA7a7FXQMeWypD9bA5i6_M%3D").build()
            }

            private fun updateCurrentMediaItem(
                mediaItem: MediaItem,
                mediaController: MediaController
            ) {
                val curIdx = mediaController.currentMediaItemIndex
                mediaController.replaceMediaItem(curIdx, mediaItem)
            }

            private fun retryToPlay(
                mediaController: MediaController
            ) {
                mediaController.prepare()
                mediaController.play()
                mediaController.playWhenReady = true
            }


            @OptIn(UnstableApi::class)
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.d(TAG, error.toString())
                //Log.d(TAG, error.errorCode.toString())
                if (error is ExoPlaybackException) {
                    //Log.d(TAG, "error EXO ${(error as ExoPlaybackException).type}")
                    //mctl.stop()
                    mctl.currentMediaItem?.let {
                        val cnt = retryCntMap.getOrDefault(it.mediaId, 0)
                        //Log.d(TAG, "retry count $cnt")
                        if (cnt < retryLimit) {
                            viewModelScope.launch {
                                retryCntMap[it.mediaId] =
                                    retryCntMap.getOrDefault(it.mediaId, 0) + 1
                                val newUri = resetMediaUri(it)
                                updateCurrentMediaItem(newUri, mctl)
                                retryToPlay(mctl)
                            }
                        } else {
                            mctl.seekToNext()
                        }
                    }
                } else {
                    mctl.seekToNext()
                }

            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                //Log.d(TAG, player.bufferedPercentage.toString())
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)
                    && events.size() == 3
                    && events.contains(Player.EVENT_MEDIA_METADATA_CHANGED)
                    && events.containsAny(
                        Player.EVENT_TIMELINE_CHANGED, Player.EVENT_MEDIA_ITEM_TRANSITION
                    )
                ) {
                    if (player.isPlaying && !counted) {
                        println("RRR ${mctl.mediaMetadata.title}")
                        mediaEntityRepo.insert(mctl.mediaMetadata.title.toString())
                        counted = true
                    }
                }

            }


            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                when (playbackState) {
                    Player.STATE_IDLE -> {
                        // Player is in the idle state
                        // This state occurs when playback is neither started nor stopped
                    }

                    Player.STATE_BUFFERING -> {
                        // Player is buffering data
                        // This state occurs when the player is preparing to play, but not playing yet
                    }

                    Player.STATE_READY -> {
                        // Player is ready to play
                        // This state occurs when the player is ready to start or resume playback
                    }

                    Player.STATE_ENDED -> {
                        // Player has ended playback
                        // This state occurs when playback has completed
                    }

                    else -> {
                        // Handle other playback states if needed
                    }
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            }
        })


    }

    private fun play(medias: List<MediaItem>) {
        mctl.run {
            when (this.isPlaying) {
                true -> {
                    //println("ASTY PLAY")
                    this.addMediaItems(medias)
                }

                else -> {
                    //println("ASTY PLAYS")
                    this.addMediaItems(medias)
                    this.prepare()
                    this.play()
                }
            }

        }
    }

    fun getCurrentPlayList() {
        if (_playList.value.isEmpty()) {
            _playList.value = MutableList<String>(mctl.mediaItemCount) { idx ->
                mctl.getMediaItemAt(idx).mediaMetadata.title.toString()
            }
        } else {
            val res = MutableList<String>(mctl.mediaItemCount) { idx ->
                mctl.getMediaItemAt(idx).mediaMetadata.title.toString()
            }.toList()
            if (res != _playList.value) {
                _playList.value = res
            } else {
            }
        }
    }

    suspend fun getPlayListId(videoId: String): String {
        val reqBody = PlayListRequestBody(videoId)
        val res = client.getPlayListIdByVideoId(reqBody)
        res.body()?.let {
            return it
        }
        return ""
    }

    @OptIn(UnstableApi::class)
    suspend fun playByPlayList(videoId: String, playListId: String) {
        val reqBody = PlayListRequestBody(videoId, playListId)
        //Log.d(TAG,"${reqBody.videoId}, ${reqBody.playlistId}")
        val res = client.getPlayList(reqBody)
        if (res.isSuccessful) {
            res.body().let { infos ->
                if (infos != null) {
                    play(preparePlaylist(infos.drop(1)))
                }
            }
        }
    }

    suspend fun playByVideoId(videoId: String): ActionResult {
        if (videoId.isEmpty()) {
            return ActionResult(false, "videoId cannot be null")
        }
        val reqBody = PlayListRequestBody(videoId)
        //(TAG, "${reqBody.videoId}, ${reqBody.playlistId}")
        val res = client.getPlayList(reqBody)
        if (res.isSuccessful) {
            res.body().let { infos ->
                if (infos != null) {
                    play(preparePlaylist(infos))
                    return ActionResult(true)
                }
            }
        }
        return ActionResult(false, "Internet error: ${res.code()}")
    }

    fun playLocal(uri: Uri) {
        val m = MediaItem.fromUri(uri)

        play(listOf(m))
    }

    fun playLocal(audio: LocalAudio) {
        val m = MediaItem.Builder()
            .setMediaId(audio.name)
            .setUri(audio.uri)
            //.setMimeType("audio/opus")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(audio.name)
                    .setArtworkUri(audio.artwork ?: lImage )
                    .setArtist(audio.artist)
                    .build()
            ).build()
        play(listOf(m))
    }

    private suspend fun preparePlaylist(infos: List<MediaInfo>): List<MediaItem> {
        return infos.map { mInfo ->
            withContext(Dispatchers.IO) {
                mediaDBService.i(
                    mInfo.videoId,
                    mInfo.title,
                    mInfo.thumbnails?.last()
                )
            }

            MediaItem.Builder()
                .setMediaId(mInfo.videoId)
                .setUri("youtube://music/${mInfo.videoId}")
                //.setMimeType("audio/opus")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(mInfo.title)
                        .setArtworkUri(Uri.parse(mInfo.thumbnails?.last()?.url ?: ""))
                        .setArtist(mInfo.info)
                        .build()
                ).build()
        }
    }

    private suspend fun getMediaById(videoId: String): String {
        val res = client.getMediaUriById(videoId)
        return res.body() ?: ""
    }

    fun playAt(idx: Int) {
        mctl.seekToDefaultPosition(idx)
    }

    fun previous() {
        mctl.seekToPrevious()
    }

    fun next() {
        mctl.seekToNext()
    }

    fun removeAt(idx: Int) {
        mctl.removeMediaItem(idx)
    }
}