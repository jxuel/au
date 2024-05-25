package com.el.mu.repo

import comelmu.Media

interface MediaEntityRepo {
    fun selectAll(): List<Media>
    fun insert(title: String)
    fun deleteById(mediaId: Long)
    fun create(title: String)
    fun selectRecentPlayed(limit: Long): List<Media>
}