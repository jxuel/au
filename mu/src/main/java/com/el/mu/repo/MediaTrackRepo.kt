package com.el.mu.repo

import comelmu.MediaTrack

interface MediaTrackRepo {
    fun insert(videoId: String, siteId: DataSite, title: String)
    fun selectByMediaId(mediaId: Long): List<MediaTrack>
}