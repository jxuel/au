package com.el.mu.repo

import com.el.mu.Database
import comelmu.MediaTrack

class MediaTrackRepoImpl(
    db: Database
): MediaTrackRepo{
    private val queries = db.mediaTrackEntityQueries
    override fun insert(videoId: String, siteId: DataSite, title: String) {
        queries.insertWithMediaTitle(videoId, title)
    }

    override fun selectByMediaId(mediaId: Long): List<MediaTrack> {
        return queries.selectByMediaId(mediaId).executeAsList()
    }
}