package com.example.myapplication.viewmodel

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession

class Media(private val context: Context) {
    init {
        val player = ExoPlayer.Builder(context).build()
        val mediaSession = MediaSession.Builder(context, player).build()
    }
}