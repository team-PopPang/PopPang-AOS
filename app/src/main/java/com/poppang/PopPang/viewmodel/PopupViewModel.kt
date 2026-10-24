package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelChildren

class PopupViewModel: ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())
    private var isLoaded = false

    fun reset() {
        viewModelScope.coroutineContext.cancelChildren()
        isLoaded = false
        _popupList.value = emptyList()
    }
    val popupList: StateFlow<List<PopupEvent>> = _popupList

    fun fetchPopupEvents(userUuid: String) {
        viewModelScope.launch {
            try {
                val response =
                    RetrofitInstance.popupApi.getPopupEvent()
                _popupList.value = response
            } catch (e: Exception) {
                isLoaded = false
            }
        }
    }
    fun fetchPopupEventsOnce(userUuid: String) {
        if (isLoaded) return
        isLoaded = true
        fetchPopupEvents(userUuid = userUuid)
    }}