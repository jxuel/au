package com.el.ytrepo.data

data class SearchResult(
    val mediaInfoList: List<MediaInfo>,
    val searchParams: List<String> = emptyList(),
    val searchQuery: String = ""
) {
}