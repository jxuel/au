package com.el.ytrepo.data

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MusicRequestBody(
    val videoId: String,
    val playlistId: String? = null,
) {
    val context: Context = Context
}

data object Context {
    data object Client {
        val clientName = "ANDROID_MUSIC"
        val clientVersion = "5.01"
    }
    val client: Client = Client
}

