package com.el.ytrepo.data

data class BrowseRequestBody(
    val browseId: String,
    val context: Context = Context.ANDROID
) {
}