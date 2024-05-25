package ytrepo

import com.el.ytrepo.YTClient
import com.el.ytrepo.data.MusicRequestBody
import com.el.ytrepo.data.PlayListRequestBody
import com.el.ytrepo.data.SearchRequestBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import okhttp3.internal.wait
import java.io.File
import kotlin.concurrent.thread

class UnitTest {

    private fun waitUntil(exec: ()->Unit, lock: Any) {
        synchronized(lock) {
            thread {
                exec()
            }
            lock.wait()
        }
    }

    fun testUri() {

        val lock = object {}
        waitUntil({
            CoroutineScope(Dispatchers.IO).launch {
                val client = YTClient()
                val p = MusicRequestBody("z9VMaLxg9Ok")
                val res = client.getMediaUriById("z9VMaLxg9Ok")
                println(res.body())


                //End wait
                synchronized(lock) {
                    lock.notify()
                }
            }
        }, lock)
    }


    fun testPlaylist() {
        val lock = object {}
        waitUntil({
            CoroutineScope(Dispatchers.IO).launch {
                val client = YTClient()
                val p = PlayListRequestBody(
                    playlistId = "OLAK5uy_mpuJ3ywnFl_3lOEkeu1C4uofTa6hkB3bE"
                )
                val res = client.getPlayList(p)
                res.body()?.forEach {
                    println(it)
                }
                println(res.code())
                //End wait
                synchronized(lock) {
                    lock.notify()
                }
            }
        }, lock)
    }


    fun testWait() {
        val lock = object {}
        waitUntil({
            val matcher = "\"videoId\":\"(\\w+)\",\"playlistId\":\"(\\w+)\"".toRegex()
            val client = YTClient()
            CoroutineScope(Dispatchers.IO).launch {
                client.searchMediaInfo(SearchRequestBody("Sunflower", "EgWKAQIYAWoMEAMQBBAKEAUQCRAV")).let { it ->
                    it.body()?.let { json ->
                        val i = json.mediaInfoList.iterator()
                        //println(json.toPrettyString())
                        while(i.hasNext()) {
                            val v = i.next()
                            //rintln(i.next())
                        }
                        //println(body)
                        File("fileName.txt").writeText(json.toString())
                        val d = matcher.findAll(json.toString())
                            println(d.count())
                        d.map {
                            //println(it.groupValues[1])
                            it.groupValues[1] + " " + it.groupValues[2]
                        }.toSet().forEach {
                            println(it  )
                        }
                    }
                }
                //End wait
                synchronized(lock) {
                    lock.notify()
                }
            }
        }, lock)
    }

}