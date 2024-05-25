package com.el.mu.repo


import com.el.mu.Database
import comelmu.Artwork

class ArtworkRepoImpl(
    private val db: Database
): ArtworkRepo {
    private val queries = db.artworkEntityQueries
    override fun selectAll(): List<Artwork> {
        //db.trackEntityQueries.insertWithMediaTitle()
        return queries.selectAll().executeAsList()
    }

    override fun insert(url: String, height: Long, width: Long, media_id: Long) {
        queries.insert(url, height, width, media_id)
    }

    override fun insert(url: String, height: Long, width: Long, title: String) {
        queries.insertWithMediaTitle(url, height, width, title)
    }

    override fun deleteById(artworkId: Long) {
        queries.deleteById(artworkId)
    }

    override fun selectByMediaId(mediaId: Long): List<Artwork> {
        return queries.selectByMediaId(mediaId).executeAsList()
    }

    override fun lastInsertId(): Long {
        return queries.lastInsertRowId().executeAsOne()
    }
}