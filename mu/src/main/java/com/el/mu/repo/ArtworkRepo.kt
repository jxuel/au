package com.el.mu.repo

import comelmu.Artwork


interface ArtworkRepo {
    fun selectAll(): List<Artwork>
    fun insert(url:String, height:Long, width:Long, media_id:Long)
    fun insert(url:String, height:Long, width:Long, title:String)
    fun deleteById(artworkId: Long)
    fun selectByMediaId(mediaId: Long): List<comelmu.Artwork>
    fun lastInsertId(): Long
}