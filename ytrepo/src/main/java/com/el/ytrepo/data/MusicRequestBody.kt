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
    data class Client(
        val clientName: String,
        val clientVersion: String,
        val userAgent: String? = null,
        val osVersion: String? = null,
        val deviceMake: String? = null,
        val deviceModel: String? = null,
        val hl: String? = null,
        val osName: String? = null,
        val timeZone: String? = null,
        val utcOffsetMinutes: Int? = null,
        val acceptHeader: String? = null
    ) {
    }
    companion object {
        val ANDROID = Context (
            Client(clientName = "ANDROID_MUSIC",
                 clientVersion = "5.01",
            )

        )
        val IOS = Context (
            Client(clientName = "IOS",
                clientVersion = "19.29.1",
                userAgent = "com.google.ios.youtube/19.29.1 (iPhone16,2; U; CPU iOS 17_5_1 like Mac OS X;)",
                osVersion = "17.5.1.21F90",
                deviceMake = "Apple",
                deviceModel = "iPhone16,2",
                hl = "en",
                osName = "iPhone",
                timeZone = "UTC",
                utcOffsetMinutes = 0,
                acceptHeader = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"
            )

        )
    }
}
/*
'client': {
    'clientName': 'IOS',
    'clientVersion': '19.29.1',
    'deviceMake': 'Apple',
    'deviceModel': 'iPhone16,2',
    'hl': 'en',
    'osName': 'iPhone',
    'osVersion': '17.5.1.21F90',
    'timeZone': 'UTC',
    'userAgent':
    'com.google.ios.youtube/19.29.1 (iPhone16,2; U; CPU iOS 17_5_1 like Mac OS X;)',
    'utcOffsetMinutes': 0

}*/
