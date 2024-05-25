package com.el.mu.repo

import com.el.mu.Database
import comelmu.SearchHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SearchHistoryRepoImpl(
    db: Database
): SearchHistoryRepo {

    lateinit var db: Database

    private val queries = db.searchHistoryEntityQueries
    
    private val _historyList = MutableStateFlow(emptyList<String>())
    val historyList: StateFlow<List<String>> = _historyList

    fun updateHistoryList(): List<String> {
        val res =  queries.selectAll().executeAsList()
            .map { it.query }

        //_historyList.value = res
        return res
    }

    override fun selectAll(): List<SearchHistory> {
        return queries.selectAll().executeAsList();
    }

    override fun delete(query:String) {
        queries.delete(query)
    }

    override fun insert(query:String) {
        queries.insert(query)
    }

    override fun update(query: String) {
        queries.update(query)

    }
}