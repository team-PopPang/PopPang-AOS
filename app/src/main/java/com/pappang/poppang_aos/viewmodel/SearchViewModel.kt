package com.pappang.poppang_aos.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pappang.poppang_aos.model.PopupEvent
import com.pappang.poppang_aos.network.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    val popupList = mutableStateOf<List<PopupEvent>>(emptyList())
    val recentQueries = mutableStateOf<List<String>>(emptyList())

    private var searchJob: Job? = null

    fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // 디바운스
            try {
                val result = RetrofitInstance.searchApi.search(query)
                popupList.value = result
            } catch (e: Exception) {
                popupList.value = emptyList()
            }
        }
    }

    fun addRecentQuery(query: String) {
        if (query.isNotBlank() && !recentQueries.value.contains(query)) {
            recentQueries.value = listOf(query) + recentQueries.value.take(4) // 최대 5개 저장
        }
    }
}
