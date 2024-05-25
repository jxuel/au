package com.el.ytrepo.data

data class StreamingData(
    val itag: Int,
    val url: String,
    val mimeType:String,
    val quality: String,
    val audioQuality:AudioQuality? = null,
)