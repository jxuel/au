package com.el.ytrepo.data

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SearchSugRequestBody(
    val input: String,
    val context: Context = Context
)
