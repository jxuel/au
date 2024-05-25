package com.el.mu.repo

import comelmu.Track


interface TrackEntityRepo {
    fun selectAll(): List<Track>
    fun lastInsertId(): Long
    fun deleteById(videoId: String)
    fun insert(videoId: String, dataSite: DataSite)
}