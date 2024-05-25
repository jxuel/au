package com.example.myapplication.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {
    // Holds our view state which the UI collects via [state]
    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> get() = _state
    // Holds the currently available home categories
    private val categories = MutableStateFlow(HomeCategory.values().asList())
    // Holds our currently selected home category
    private val selectedCategory = MutableStateFlow(HomeCategory.Discover)
    private val refreshing = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            combine(
                categories,
                selectedCategory,
                refreshing
            ) {categories, selectedCategory, refreshing ->
                HomeViewState(
                    selectedHomeCategory = selectedCategory,
                    homeCategories = categories,
                    errorMessage = null)
            }.catch { throwable ->
                // TODO: emit a UI error here. For now we'll just rethrow
                throw throwable
            }.collect {
                _state.value = it
            }
        }
    }
    fun onHomeCategorySelected(category: HomeCategory) {
        selectedCategory.value = category
    }

    private fun refresh(force: Boolean) {
        viewModelScope.launch {
            runCatching {
                refreshing.value = true
                //podcastsRepository.updatePodcasts(force)
            }
            // TODO: look at result of runCatching and show any errors

            refreshing.value = false
        }
    }

}

enum class HomeCategory {
    Library, Discover
}

data class HomeViewState(
   // val featuredPodcasts: PersistentList<PodcastWithExtraInfo> = persistentListOf(),
    //val refreshing: Boolean = false,
    val selectedHomeCategory: HomeCategory = HomeCategory.Discover,
    val homeCategories: List<HomeCategory> = emptyList(),
    val errorMessage: String? = null
)