package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PopupProgressViewModel: ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())
    private var isLoaded = false
    val popupprogressList: StateFlow<List<PopupEvent>> = _popupList

    fun fetchPopupProgressEvents() {
        viewModelScope.launch {
            try {
                val response =
                    RetrofitInstance.popupProgressApi.getPopupProgressEvent("all") // API에 맞게 수정
                _popupList.value = response
            } catch (e: Exception) {
            }
        }
    }
    fun fetchPopupProgressEventsOnce() {
        if (isLoaded) return
        isLoaded = true
        fetchPopupProgressEvents()
    }
}