package com.el.ytrepo.data

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SearchRequestBody (
    val query: String,
    val params: String? = null) {
    val context: Context = Context.ANDROID
}