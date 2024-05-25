package com.el.mu.repo


import com.el.mu.Database
import comelmu.Track

class TrackEntityRepoImpl(
    private val db: Database
): TrackEntityRepo {
    private val queries = db.trackEntityQueries
    override fun selectAll(): List<Track> {
        //db.trackEntityQueries.insertWithMediaTitle()
        return queries.selectAll().executeAsList()
    }

    override fun insert(videoId: String, dataSite: DataSite) {
        queries.instert(videoId, dataSite.ordinal.toLong())
    }

    override fun deleteById(videoId: String) {
        queries.deleteById(videoId)
    }

    override fun lastInsertId(): Long {
        return queries.lastInsertRowId().executeAsOne()
    }
}