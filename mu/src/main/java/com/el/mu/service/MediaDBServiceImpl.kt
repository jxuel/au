package com.el.mu.service

import app.cash.sqldelight.db.SqlDriver
import com.el.mu.Database
import com.el.mu.repo.ArtworkRepo
import com.el.mu.repo.DataSite
import com.el.mu.repo.MediaEntityRepo
import com.el.mu.repo.MediaTrackRepo
import com.el.mu.repo.TrackEntityRepo
import com.el.ytrepo.data.Thumbnail
import javax.inject.Inject

class MediaDBServiceImpl @Inject constructor(
    private val mediaEntityRepo: MediaEntityRepo,
    private val trackEntityRepo: TrackEntityRepo,
    private val artworkRepo: ArtworkRepo,
    private val mediaTrackRepo: MediaTrackRepo,
    private val driver: SqlDriver
) : MediaDBService {
    private val db: Database = Database(driver)

    fun abs(title: String, thumbnail: Thumbnail) {
        mediaEntityRepo.create(title)
        //trackEntityRepo.insert(mInfo.videoId, DataSite.youtube, mInfo.title)
        artworkRepo.insert(
            thumbnail.url,
            thumbnail.width.toLong(),
            thumbnail.height.toLong(),
            title
        )
    }
    fun a(title: String) {
        mediaEntityRepo.create(title)
        //trackEntityRepo.insert(mInfo.videoId, DataSite.youtube, mInfo.title)
    }
    fun t(videoId: String) {
        trackEntityRepo.insert(videoId, DataSite.youtube)
    }

    fun b(videoId: String, dataSite: DataSite, title: String) {
        mediaTrackRepo.insert(videoId, dataSite, title)
    }

    fun ab(title: String, thumbnail: Thumbnail) {
        artworkRepo.insert(
            thumbnail.url,
            thumbnail.width.toLong(),
            thumbnail.height.toLong(),
            title
        )
    }

    override fun s(title: String, thumbnail: Thumbnail?)  {
        db.transaction {
            a(title)
            if(thumbnail != null) {
                ab(title, thumbnail)
            }
        }
    }

    override fun i( videoId: String, title: String, thumbnail: Thumbnail?)  {
        db.transaction {
            a(title)
            t(videoId)
            b(videoId, DataSite.youtube, title)
            if(thumbnail != null) {
                ab(title, thumbnail)
            }
        }
    }

}