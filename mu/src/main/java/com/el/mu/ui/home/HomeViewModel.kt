package com.el.mu.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.el.mu.data.MediaItem
import com.el.mu.repo.ArtworkRepo
import com.el.mu.repo.MediaEntityRepo
import com.el.mu.repo.MediaTrackRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaEntityRepo: MediaEntityRepo,
    private val mediaTrackRepo: MediaTrackRepo,
    private val artworkRepo: ArtworkRepo

): ViewModel() {
    private val _uiState = MutableStateFlow(MediaItem())
    val uiState: StateFlow<MediaItem> = _uiState as StateFlow<MediaItem>

    private val _list = MutableStateFlow(a())
    val list: StateFlow<List<SearchRenderItem>> = _list

    //private val _videoIds = MutableStateFlow(MutableList<String>())
    //val videoIds: StateFlow<String> = _videoIds as StateFlow<String>

    companion object {
        private val TAG = HomeViewModel::class.java.name
    }

    init {
        //fetchList()
        /*CoroutineScope(Dispatchers.IO).launch{
            _list.value = search("Sunflower")
        }*/


    }

    fun a(): List<SearchRenderItem> {
        return mediaEntityRepo.selectRecentPlayed(20L).map { media ->
            SearchRenderItem(
                media.title,
                subTitle = media.playCnt.toString(),
                videoId = mediaTrackRepo.selectByMediaId(media.id).firstOrNull()?.trackId?:"",
                image = Uri.parse(artworkRepo.selectByMediaId(media.id).firstOrNull()?.url ?: "")
            )
        }
    }

    fun fetchList() {
        _uiState.value = MediaItem()
    }


}