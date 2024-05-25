package com.el.mu.ui.search

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.el.mu.repo.SearchHistoryRepo
import com.el.mu.ui.home.SearchRenderItem
import com.el.ytrepo.YTClient
import com.el.ytrepo.data.SearchRequestBody
import com.el.ytrepo.data.SearchSugRequestBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchHistoryRepo: SearchHistoryRepo
) : ViewModel() {

    companion object {
        private val TAG = SearchViewModel::class.java.name
    }

    private val _historyList = MutableStateFlow(updateHistoryList())
    val historyList: StateFlow<List<String>> = _historyList

    private val _sug= MutableStateFlow(ArrayList<String>().toList())
    val sug: StateFlow<List<String>> = _sug

    private val _sParams = MutableStateFlow("")
    val sParams: StateFlow<String> = _sParams


    fun updateHistoryList(): List<String> {
        val res = searchHistoryRepo.selectAll()
            .map { it.query }
        //_historyList.value = res
        return res
    }

    private val client = YTClient()

    private val _list = MutableStateFlow(ArrayList<SearchRenderItem>())
    val list: StateFlow<ArrayList<SearchRenderItem>> =
        _list as StateFlow<ArrayList<SearchRenderItem>>

    suspend fun searchBrief(query: String) {
        val req = SearchRequestBody(query)
        val res = client.searchMediaInfo(req)

        if (!res.isSuccessful) {
            return
        }
        val result = ArrayList<SearchRenderItem>(3)

        res.body()?.mediaInfoList?.map {
            SearchRenderItem(it.title, it.videoId, subTitle = it.info,
                image = Uri.parse(
                    it.thumbnails?.lastIndex?.let { it1 ->
                        it.thumbnails?.get(it1)?.url
                    } ?: ""))
        }?.let {
            result.addAll(it)
        }

        _sParams.value = res.body()?.searchParams?.get(0) ?: ""

        if (result.isNotEmpty()) {
            _list.value = result
        }
    }

    suspend fun search(query: String) {
        searchBrief(query)
        val idx = _historyList.value.indexOf(query)
        // DB update
        if (idx == -1) {
            searchHistoryRepo.insert(query)
        } else {
            searchHistoryRepo.update(query)
        }
        //UI update
        if (idx != 0) {
            _historyList.value = _historyList.value.toMutableList().apply {
                if (idx != -1) {
                    removeAt(idx)
                }
                add(0, query)
            }

        }

    }

    suspend fun search(query: String, param:String) {
        searchMore(query, param)
        val idx = _historyList.value.indexOf(query)
        // DB update
        if (idx == -1) {
            searchHistoryRepo.insert(query)
        } else {
            searchHistoryRepo.update(query)
        }
        //UI update
        if (idx != 0) {
            _historyList.value = _historyList.value.toMutableList().apply {
                if (idx != -1) {
                    removeAt(idx)
                }
                add(0, query)
            }

        }

    }

    suspend fun searchMore(query: String, param: String) {
        val req = SearchRequestBody(query, param)
        val res = client.searchMediaInfo(req)

        if (!res.isSuccessful) {
            return
        }
        val result = ArrayList<SearchRenderItem>(3)

        res.body()?.mediaInfoList?.map {
            SearchRenderItem(it.title, it.videoId, subTitle = it.info,
                image = Uri.parse(
                    it.thumbnails?.lastIndex?.let { it1 ->
                        it.thumbnails?.get(it1)?.url
                    } ?: ""))
        }?.let {
            result.addAll(it)
        }

        _sParams.value = res.body()?.searchParams?.get(0) ?: ""

        if (result.isNotEmpty()) {
            _list.value = result
        }

    }

    suspend fun searchSuggestions(query: String) {
        val req = SearchSugRequestBody(query)
        val res = client.searchSuggestion(req)
        if (!res.isSuccessful) {
            return
        }
        res.body()?.let {
            _sug.value = it.map {s->
                s.query
            }
        }
    }

    suspend fun searchRegx(keyword: String): HashSet<String> {
        val matcher = "videoId\":\"(\\w+)\"".toRegex()
        client.searchMedia(SearchRequestBody("Sunflower")).let { it ->
            //Log.d(TAG, it.code().toString())
            it.body()?.toString()?.let { body ->
                //Log.d(this.javaClass.name, body.substring(191583))
                val l = matcher.findAll(body).map {
                    it.groupValues[1]
                }
                return l.toHashSet()
            }
        }
        return HashSet()
    }

    fun deleteRecordByIdx(idx: Int) {
        _historyList.value = _historyList.value.toMutableList().apply {
            searchHistoryRepo.delete(removeAt(idx))
        }
    }

    fun cleanSug() {
        _sug.value = emptyList()
    }
}