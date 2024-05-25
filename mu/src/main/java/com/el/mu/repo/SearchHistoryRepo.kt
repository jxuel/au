package com.el.mu.repo

import comelmu.SearchHistory

interface SearchHistoryRepo {
    fun selectAll(): List<SearchHistory>

    fun delete(query:String)

    fun insert(query:String)

    fun update(query: String)
}