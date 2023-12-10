package com.example.myapplication

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerControlView
import com.example.mservice.PlaybackService
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : ComponentActivity() {
    lateinit var playerView:PlayerControlView
    @androidx.annotation.OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            playerView = PlayerControlView(applicationContext)
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
                playerView.setPlayer(controllerFuture.get())
            },
            MoreExecutors.directExecutor()
        )
    }


}