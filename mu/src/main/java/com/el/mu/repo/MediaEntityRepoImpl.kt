package com.el.mu.repo

import com.el.mu.Database
import comelmu.Media

class MediaEntityRepoImpl(
    db: Database
): MediaEntityRepo {
    private val queries = db.mediaEntityQueries

    override fun selectAll(): List<Media> {
        return queries.selectAll().executeAsList()
    }

    override fun insert(title: String) {
        queries.insert(title)
    }

    override fun deleteById(mediaId: Long) {
        queries.deleteById(mediaId)
    }

    override fun create(title: String) {
        queries.create(title)
    }

    override fun selectRecentPlayed(limit: Long): List<Media> {
        return queries.selectRecentPlayed(limit).executeAsList()
    }

}