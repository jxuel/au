package com.el.ytrepo.data

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MusicRequestBody(
    val videoId: String,
    val playlistId: String? = null,
) {
    val context: Context = Context.IOS
}

data class Context (
    val client: Client
) {
    data class Client (
        val clientName: String,
        val clientVersion: String,
    ) {
    }
    companion object {
        val ANDROID = Context (
            Client(clientName = "ANDROID_MUSIC",
                 clientVersion = "5.01"
            )

        )
        val IOS = Context (
            Client(clientName = "IOS",
                clientVersion = "19.29.1"
            )

        )
    }
}

