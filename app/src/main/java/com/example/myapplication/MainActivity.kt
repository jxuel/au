package com.example.myapplication

import android.content.ComponentName
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.mservice.PlaybackService
import com.example.myapplication.ui.theme.MainTheme
import com.google.accompanist.adaptive.calculateDisplayFeatures
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : ComponentActivity() {
    //lateinit var playerView:PlayerView

    @kotlin.OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT, detectDarkMode = { true })
        )

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val displayFeatures = calculateDisplayFeatures(this)
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                MainTheme {
                    MusicApp(windowSizeClass, displayFeatures)
                }
            }
        }
    }


    @OptIn(UnstableApi::class) override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                // Call controllerFuture.get() to retrieve the MediaController.
                // MediaController implements the Player interface, so it can be
                // attached to the PlayerView UI component.
                //playerView.setPlayer(controllerFuture.get())
                /*val mediaItem = MediaItem.fromUri("https://rr4---sn-p5qddn7d.googlevideo.com/videoplayback?expire=1702195345&ei=MRx1Zf-pL8at_9EPwP-x0A0&ip=73.212.187.42&id=o-ANWYOG5lECf9UmR7tw1ZNWNuvwcCc-bjGFkBv-0V9H7U&itag=139&source=youtube&requiressl=yes&xpc=EgVo2aDSNQ%3D%3D&mh=L8&mm=31%2C29&mn=sn-p5qddn7d%2Csn-p5qlsndr&ms=au%2Crdu&mv=m&mvi=4&pl=16&gcr=us&initcwndbps=1273750&vprv=1&mime=audio%2Fmp4&gir=yes&clen=963202&dur=157.559&lmt=1603797423730063&mt=1702173399&fvip=1&keepalive=yes&fexp=24007246&c=ANDROID_MUSIC&txp=5531432&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cxpc%2Cgcr%2Cvprv%2Cmime%2Cgir%2Cclen%2Cdur%2Clmt&sig=ANLwegAwRAIgHXSw8RxzWMj6ZmKlTMyUBY-a1v_B2zVgZDo1cEN9wO4CIDr9xoJYY-FYrdjeHFOGG7pVMhHS748WAnE6d6yLVCfF&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AM8Gb2swRQIgcXLD50zj0J_-yw5IDEnA5PTL-Tl-UeEQIUBnzvYEPHgCIQDY5NDn02mdOoHeiHcq_0F9LylhuFy4nj0wEj46TCNGJg%3D%3D")
                controllerFuture.get().let {
                    it.setMediaItem(mediaItem)
                    it.prepare()
                    it.play()
                }*/
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}