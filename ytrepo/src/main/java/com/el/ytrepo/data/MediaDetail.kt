package com.el.ytrepo.data

data class MediaDetail(
    val mediaInfo: MediaInfo,
    val streamingData: List<StreamingData>? = null,
)

