package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.datastore.SearchQueryDataStore
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val searchQueryDataStore: SearchQueryDataStore) : ViewModel() {
    val popupList = mutableStateOf<List<PopupEvent>>(emptyList())
    val recentQueries = mutableStateOf<List<String>>(emptyList())
    private val _searchedPopups = MutableStateFlow<List<PopupEvent>>(emptyList())

    val searchedPopups: StateFlow<List<PopupEvent>> = _searchedPopups

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            searchQueryDataStore.queriesFlow.collect { queries ->
                recentQueries.value = queries.toList()
            }
        }
    }

    fun search(query: String) {
        popupList.value = emptyList()
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                val result = RetrofitInstance.searchApi.search(query)
                popupList.value = result
            } catch (e: Exception) {
                popupList.value = emptyList()
            }
        }
    }

    fun addRecentQuery(query: String) {
        viewModelScope.launch {
            if (query.isNotBlank()) {
                searchQueryDataStore.addQuery(query)
            }
        }
    }

    fun removeRecentQuery(query: String) {
        viewModelScope.launch {
            searchQueryDataStore.removeQuery(query)
            recentQueries.value = searchQueryDataStore.getQueries()
        }
    }

}
