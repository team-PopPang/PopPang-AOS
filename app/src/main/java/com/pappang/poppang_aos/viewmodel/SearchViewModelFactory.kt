package com.pappang.poppang_aos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pappang.poppang_aos.datastore.SearchQueryDataStore

class SearchViewModelFactory(
    private val searchQueryDataStore: SearchQueryDataStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(searchQueryDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}