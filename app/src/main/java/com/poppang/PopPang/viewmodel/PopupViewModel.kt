package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PopupViewModel: ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())
    private var isLoaded = false
    val popupList: StateFlow<List<PopupEvent>> = _popupList

    fun fetchPopupEvents(userUuid: String) {
        viewModelScope.launch {
            try {
                val response =
                    RetrofitInstance.popupApi.getPopupEvent(userUuid,"all")
                _popupList.value = response
            } catch (e: Exception) {
            }
        }
    }
    fun fetchPopupEventsOnce(userUuid: String) {
        if (isLoaded) return
        isLoaded = true
        fetchPopupEvents(userUuid = userUuid)
    }}