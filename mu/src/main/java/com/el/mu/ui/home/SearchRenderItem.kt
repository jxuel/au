package com.el.mu.ui.home

import android.net.Uri

data class SearchRenderItem(
    val title: String,
    val videoId: String = "",
    val playListId: String = "",
    val subTitle: String = "",
    val image:Uri = Uri.EMPTY
) {
}