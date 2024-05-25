package com.el.ytrepo

import com.el.ytrepo.data.AudioQuality
import com.el.ytrepo.data.BrowseRequestBody
import com.el.ytrepo.data.MediaDetail
import com.el.ytrepo.data.MediaInfo
import com.el.ytrepo.data.MusicRequestBody
import com.el.ytrepo.data.PlayListRequestBody
import com.el.ytrepo.data.SearchRequestBody
import com.el.ytrepo.data.SearchResult
import com.el.ytrepo.data.SearchSugRequestBody
import com.el.ytrepo.data.SearchSuggest
import com.el.ytrepo.data.StreamingData
import com.el.ytrepo.data.Thumbnail
import com.fasterxml.jackson.databind.JsonNode
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class YTClient {
    companion object {
        private const val url = "https://music.youtube.com/youtubei/v1/"
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        val httpClient = OkHttpClient.Builder()

        //  .addInterceptor(logging)
        private var retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(JacksonConverterFactory.create())
            .client(httpClient.build())
            .build()

        var service: YTInterface = retrofit.create(YTInterface::class.java)

        private val itag251UriRegex = "itag\":251,\"url\":\"(.*?)\"".toRegex()
        private val playListIdRegex = "\"playlistId\":\"(.*?)\"".toRegex()
    }

    suspend fun getPlayList(reqBody: PlayListRequestBody): Response<List<MediaInfo>> {
        val res = service.next(reqBody)
        if (res.isSuccessful) {
            res.let {
                it.body()!!.let { data ->
                    val list = data.get("contents")
                        .get("singleColumnMusicWatchNextResultsRenderer")
                        .get("tabbedRenderer")
                        .get("watchNextTabbedResultsRenderer")
                        .get("tabs")
                        .get(0)
                        .get("tabRenderer")
                        .get("content")
                        .get("musicQueueRenderer")
                        .get("content")
                        .get("playlistPanelRenderer")
                        .get("contents")
                    val out = list.filter { l ->
                        l.has("playlistPanelVideoRenderer")
                    }.map { it ->
                        val info = it.get("playlistPanelVideoRenderer")
                        val title: String = info
                            .get("title")
                            .get("runs")
                            .get(0)
                            .get("text").asText()
                        val artistLength =
                            info.get("longBylineText").get("runs").fold("") { base, ele ->
                                base + " " + ele.get("text").asText()
                            }.trim()
                        val videoId = info.get("videoId").asText()
                        val thumbnails = info.get("thumbnail")
                            .get("thumbnails")
                            .map { thumb ->
                                Thumbnail(
                                    thumb.get("url").asText(),
                                    thumb.get("width").asInt(),
                                    thumb.get("height").asInt()
                                )
                            }
                        MediaInfo(title, artistLength, videoId, thumbnails)
                    }
                    return Response.success(out, it.raw())
                }
            }
        } else {
            return Response.error(res.errorBody()!!, res.raw())
        }
    }

    suspend fun getPlayListIdByVideoId(reqBody: PlayListRequestBody): Response<String> {
        val res = service.next(reqBody)
        if (res.isSuccessful) {
            res.let {
                it.body()!!.let { data ->
                    val match = playListIdRegex.find(data.toString())
                    return Response.success(match?.groupValues?.last(), it.raw())
                }
            }
        } else {
            return Response.error(res.errorBody()!!, res.raw())
        }
    }

    suspend fun getPlayList(reqBody: String) = service.next(reqBody)

    suspend fun searchMedia(reqBody: SearchRequestBody): Response<JsonNode> {
        val res = service.search(reqBody)
        return res
    }

    suspend fun searchMediaInfo(reqBody: SearchRequestBody): Response<SearchResult> {
        val res = service.search(reqBody)
        if (res.isSuccessful) {
            res.body()!!.let { data ->
                val sectionList = data
                    .get("contents")
                    .get("tabbedSearchResultsRenderer")
                    .get("tabs")
                    .filter { tab ->
                        tab.get("tabRenderer")
                            .get("selected").asBoolean()
                    }[0]
                    .get("tabRenderer")
                    .get("content")
                    .get("sectionListRenderer")
                val list = sectionList
                    .get("contents")
                    .filter { render ->
                        render.get("musicShelfRenderer")
                            ?.get("title")
                            ?.get("runs")
                            ?.get(0)
                            ?.get("text")?.asText().equals("Songs") || reqBody.params != null
                    }[0]
                    .get("musicShelfRenderer")
                    .get("contents")
                val mediaInfoList = list.map { song ->
                    val info = song.get("musicTwoColumnItemRenderer")
                    val title: String = info
                        .get("title")
                        .get("runs")
                        .get(0)
                        .get("text").asText()
                    val artistLength =
                        info.get("subtitle").get("runs").fold("") { base, ele ->
                            base + " " + ele.get("text").asText()
                        }.trim()
                    val videoId = info.get("navigationEndpoint")
                        .get("watchEndpoint")
                        .get("videoId").asText()
                    val thumbnails = info.get("thumbnail")
                        .get("musicThumbnailRenderer")
                        .get("thumbnail")
                        .get("thumbnails")
                        .map { thumb ->
                            Thumbnail(
                                thumb.get("url").asText(),
                                thumb.get("width").asInt(),
                                thumb.get("height").asInt()
                            )
                        }
                    MediaInfo(title, artistLength, videoId, thumbnails)
                }
                val moreSearch = sectionList.get("header")
                    .get("chipCloudRenderer")
                    .get("chips")
                    .filter { chipRender ->
                        chipRender.get("chipCloudChipRenderer")
                            ?.get("text")
                            ?.get("runs")
                            ?.get(0)
                            ?.get("text")?.asText().equals("Songs") || reqBody.params != null
                    }[0]
                    .get("chipCloudChipRenderer")
                    .get("navigationEndpoint")
                    .get("searchEndpoint")
                val searchQuery = moreSearch.get("query").asText()
                val searchParam = listOf<String>(moreSearch.get("params")?.asText() ?: "")
                val out = SearchResult(mediaInfoList, searchParam, searchQuery)
                return Response.success(out, res.raw())
            }
        } else {
            return Response.error(res.errorBody()!!, res.raw())
        }
    }

    suspend fun searchMedia(reqBody: String) = service.search(reqBody)
    suspend fun getMediaById(videoId: String, playListId: String): Response<MediaDetail> {
        val reqBody = MusicRequestBody(videoId, playListId)
        val res = service.player(reqBody)
        if (res.isSuccessful) {
            res.let {
                it.body()!!.let { data ->
                    val formats = data
                        .get("streamingData")
                        .get("adaptiveFormats")
                    val fList = formats.map { fmt ->
                        StreamingData(
                            fmt.get("itag").asInt(),
                            fmt.get("url").asText(),
                            fmt.get("mimeType").asText(),
                            fmt.get("quality").asText(),
                            fmt.get("audioQuality")?.asText()
                                ?.let { it1 -> AudioQuality.valueOf(it1) }
                        )
                    }
                    val info = data
                        .get("videoDetails")
                    val title = info.get("title").asText()
                    val author = info.get("author").asText()
                    val videoId = info.get("videoId").asText()
                    val thumbnails =
                        info.get("thumbnail")
                            .get("thumbnails")
                            .map { thumb ->
                                Thumbnail(
                                    thumb.get("url").asText(),
                                    thumb.get("width").asInt(),
                                    thumb.get("height").asInt()
                                )
                            }
                    val mediaInfo = MediaInfo(title, author, videoId, thumbnails)
                    val mediaDetail = MediaDetail(mediaInfo, fList)
                    return Response.success(mediaDetail, res.raw())
                }
            }
        } else {
            return Response.error(res.errorBody()!!, res.raw())
        }
    }

    suspend fun getMediaById(videoId: String): Response<MediaDetail> {
        val reqBody = MusicRequestBody(videoId)
        val res = service.player(reqBody)
        if (res.isSuccessful) {
            res.let {
                it.body()!!.let { data ->
                    val formats = data
                        .get("streamingData")
                        .get("adaptiveFormats")
                    val fList = formats.map { fmt ->
                        StreamingData(
                            fmt.get("itag").asInt(),
                            fmt.get("url").asText(),
                            fmt.get("mimeType").asText(),
                            fmt.get("quality").asText(),
                            fmt.get("audioQuality")?.asText()
                                ?.let { it1 -> AudioQuality.valueOf(it1) }
                        )
                    }
                    val info = data
                        .get("videoDetails")
                    val title = info.get("title").asText()
                    val author = info.get("author").asText()
                    val videoId = info.get("videoId").asText()
                    val thumbnails =
                        info.get("thumbnail")
                            .get("thumbnails")
                            .map { thumb ->
                                Thumbnail(
                                    thumb.get("url").asText(),
                                    thumb.get("width").asInt(),
                                    thumb.get("height").asInt()
                                )
                            }
                    val mediaInfo = MediaInfo(title, author, videoId, thumbnails)
                    val mediaDetail = MediaDetail(mediaInfo, fList)
                    return Response.success(mediaDetail, res.raw())
                }
            }
        } else {
            return Response.error(res.errorBody()!!, res.raw())
        }
    }

    suspend fun getMediaUriById(videoId: String): Response<String> {
        val reqBody = MusicRequestBody(videoId)
        val res = service.player(reqBody)
        if (res.isSuccessful) {
            res.let {
                it.body()!!.let { data ->
                    itag251UriRegex.find(data.toString())?.let { matchRes ->
                        return Response.success(matchRes.groupValues[1], res.raw())
                    }
                }
            }
            return Response.success("", res.raw())
        } else {
            return Response.error(res.errorBody()!!, res.raw())
        }
    }

    suspend fun searchSuggestion(reqBody: SearchSugRequestBody): Response<List<SearchSuggest>> {
        val res = service.searchSug(reqBody)
        if (res.isSuccessful) {
            res.body()!!.let { data ->
                try {
                    val suggestionList = data
                        .get("contents")[0]
                        .get("searchSuggestionsSectionRenderer")
                        .get("contents")
                    val out = suggestionList.map { sugRender ->
                        SearchSuggest(sugRender
                            .get("searchSuggestionRenderer")
                            .get("suggestion")
                            .get("runs").fold("") { base, ele ->
                                base + ele.get("text").asText()
                            })
                    }
                    return Response.success(out, res.raw())
                } catch (ex: NullPointerException) {
                    return Response.success(emptyList(), res.raw())
                }
            }
        } else {
            return Response.error(res.errorBody()!!, res.raw())
        }
    }

    suspend fun getQuickPick(): Response<SearchResult> {
        val reqBody = BrowseRequestBody("FEmusic_home")
        val res = service.browse(reqBody)
        if (res.isSuccessful) {
            res.body()!!.let { data ->
                val sectionList = data
                    .get("contents")[0]
                    .get("singleColumnBrowseResultsRenderer")
                    .get("tabs")[0]
                    .get("tabRenderer")
                    .get("content")
                    .get("sectionListRenderer")
                    .get("contents")
                val musicRenderList = sectionList.filter { render ->
                    render?.get("header")
                        ?.get("musicCarouselShelfBasicHeaderRenderer")
                        ?.get("title")
                        ?.get("runs")
                        ?.get(0)
                        ?.get("text")?.asText()?.contains("Quick picks") ?: false
                }
                if (musicRenderList.isNotEmpty()) {
                    val mediaInfoList = musicRenderList[0]
                        .get("contents")
                        .map { song ->
                            val info = song.get("musicTwoColumnItemRenderer")
                            val title: String = info
                                .get("title")
                                .get("runs")
                                .get(0)
                                .get("text").asText()
                            val artistLength =
                                info.get("subtitle").get("runs").fold("") { base, ele ->
                                    base + " " + ele.get("text").asText()
                                }.trim()
                            val videoId = info.get("navigationEndpoint")
                                .get("watchEndpoint")
                                .get("videoId").asText()
                            val thumbnails = info.get("thumbnail")
                                .get("musicThumbnailRenderer")
                                .get("thumbnail")
                                .get("thumbnails")
                                .map { thumb ->
                                    Thumbnail(
                                        thumb.get("url").asText(),
                                        thumb.get("width").asInt(),
                                        thumb.get("height").asInt()
                                    )
                                }
                            MediaInfo(title, artistLength, videoId, thumbnails)
                        }
                    val out = SearchResult(mediaInfoList, listOf("searchParam"), "searchQuery")
                    return Response.success(out, res.raw())
                }else {
                    return Response.error(res.raw().body!!, res.raw())
                }
            }
        } else {
            return Response.error(res.errorBody()!!, res.raw())
        }
    }
}