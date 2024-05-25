package com.el.mu.service

import com.el.ytrepo.data.Thumbnail

interface MediaDBService {

    fun s(title: String, thumbnail: Thumbnail?)
    fun i(videoId: String, title: String, thumbnail: Thumbnail?)
}