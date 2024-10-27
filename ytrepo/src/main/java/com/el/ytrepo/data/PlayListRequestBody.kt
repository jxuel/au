package com.el.ytrepo.data

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class PlayListRequestBody (
    var videoId: String? = null,
    var playlistId: String? = null,
    val tunerSettingValue: String? = null
) {
    val context: Context = Context.ANDROID
}