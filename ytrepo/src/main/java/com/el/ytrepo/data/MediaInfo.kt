package com.el.ytrepo.data

data class MediaInfo(
    val title:String,
    val info:String,
    val videoId:String,
    val thumbnails: List<Thumbnail> ? = null
) {

}