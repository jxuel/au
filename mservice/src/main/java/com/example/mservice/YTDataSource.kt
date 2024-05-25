package com.example.mservice

import android.net.Uri
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import com.el.ytrepo.YTClient
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@UnstableApi
class YTDataSource(wrappedDataSource: DataSource) : DataSource {
    companion object {
        private const val RESOLVABLE_SCHEME = "youtube"
        private val client = YTClient()
        private val cache = ConcurrentHashMap<String,MediaURICache>()
    }

    class Factory(wrappedFactory: DataSource.Factory) : DataSource.Factory {
        private val wrappedFactory: DataSource.Factory

        init {
            this.wrappedFactory = wrappedFactory
        }

        override fun createDataSource(): DataSource {
            return YTDataSource(wrappedFactory.createDataSource())
        }
    }

    private val wrappedDataSource: DataSource

    init {
        this.wrappedDataSource = wrappedDataSource
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return wrappedDataSource.read(buffer, offset, length)
    }

    override fun addTransferListener(transferListener: TransferListener) {
        wrappedDataSource.addTransferListener(transferListener)
    }


    @Throws(IOException::class)
    override fun open(dataSpec: DataSpec): Long {
        var dataSpec = dataSpec

        if (RESOLVABLE_SCHEME == dataSpec.uri.scheme) {
            runBlocking {
               //Log.d("DDF", "Start")
                val resolvedUrl: Uri = resolveUri(dataSpec.uri)
                //Log.d("DDF", "End")
                Log.d("DDF", resolvedUrl.toString())
                dataSpec = dataSpec.withUri(resolvedUrl)
            }
        }
        return wrappedDataSource.open(dataSpec)
    }

    override fun getUri(): Uri? {
        return wrappedDataSource.uri
    }

    private suspend fun resolveUri(uri: Uri): Uri {
        //Log.d("FDF", "MAP ${cache.size}")
        uri.lastPathSegment?.let {videoId->
            var cached = cache[videoId]
            if(cached == null
                || cache[videoId]!!.createdAt + cache[videoId]!!.expireIn < System.currentTimeMillis()) {
                //Log.d("FDF", "NEW $videoId")
                //Log.d("FDF", "NEW ${cache.contains(videoId)}")
                val url = client.getMediaById(videoId).body()?.streamingData?.last()?.url ?: ""
                if (url.isNotEmpty()) {
                    cached = MediaURICache(url, 3600000, System.currentTimeMillis())
                    cache[videoId] = cached
                }
            }
            //Log.d("FDF", "CACHE ${cache[videoId]!!.createdAt + cache[videoId]!!.expireIn} ${System.currentTimeMillis()}")
            cached
        }?.let { mediaLink ->
            return Uri.parse(mediaLink.uri)
        }
        return Uri.EMPTY
        //return Uri.parse("https://rr4---sn-p5qddn7d.googlevideo.com/videoplayback?expire=1703770544&ei=UCWNZdKHDMmH_9EPvv290A8&ip=73.212.187.42&id=o-AL7Gysgf5CvCQlPMwshpzTVlwWveBMOQwI36e9ZXF1FF&itag=251&source=youtube&requiressl=yes&xpc=EgVo2aDSNQ%3D%3D&mh=L8&mm=31%2C26&mn=sn-p5qddn7d%2Csn-ab5sznzk&ms=au%2Conr&mv=m&mvi=4&pl=15&gcr=us&initcwndbps=1560000&vprv=1&mime=audio%2Fwebm&gir=yes&clen=2763593&dur=157.581&lmt=1603797422528340&mt=1703748542&fvip=1&keepalive=yes&fexp=24007246&c=ANDROID_MUSIC&txp=5531432&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cxpc%2Cgcr%2Cvprv%2Cmime%2Cgir%2Cclen%2Cdur%2Clmt&sig=AJfQdSswRQIgOmSLd4NJqvFyMIFVHocFiFZtH7yWbneQVnjRn08Q6iMCIQDT49j743smOd_rPVfPl84xQ80BltfzhTsPRGwmk2FpMA%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AAO5W4owRAIgXrQs3nFJPh31FtLYY5sDxr_5o5rTgfV1P3lgiauAHL8CIAQWsus4iY48W3YoBsLBDrI0CaSsMgLabsVig1_RlrQg")
    }

    @Throws(IOException::class)
    override fun close() {
        wrappedDataSource.close()
    }
}

data class MediaURICache(val uri:String, val expireIn:Long, val createdAt: Long)