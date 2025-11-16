package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val _popups = MutableStateFlow<List<PopupEvent>>(emptyList())
    val popups: StateFlow<List<PopupEvent>> = _popups.asStateFlow()

    private val _query = MutableStateFlow("")

    val query: StateFlow<String> = _query
    private val _searchedPopups = MutableStateFlow<List<PopupEvent>>(emptyList())
    val searchedPopups: StateFlow<List<PopupEvent>> = _searchedPopups

    private var searchJob: Job? = null

    val popupMapById: StateFlow<Map<Int, PopupEvent>> =
        _popups
            .map { list -> list.associateBy { it.popupUuid.hashCode() } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _iconSizeDp = MutableStateFlow(48)
    val iconSizeDp: StateFlow<Int> = _iconSizeDp.asStateFlow()

    private val _cornerDp = MutableStateFlow(8)
    val cornerDp: StateFlow<Int> = _cornerDp.asStateFlow()

    fun setPopups(list: List<PopupEvent>) {
        _popups.value = list
    }

    fun setQuery(query: String) {
        _query.value = query
    }

    fun search(query: String) {
        _searchedPopups.value = emptyList()
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                val result = RetrofitInstance.searchApi.search(query)
                _searchedPopups.value = result
            } catch (e: Exception) {
                _searchedPopups.value = emptyList()
            }
        }
    }
}
