package com.pappang.poppang_aos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pappang.poppang_aos.model.PopupEvent
import com.pappang.poppang_aos.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PopupComingViewModel: ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())
    private var isLoaded = false
    val popupcomingList: StateFlow<List<PopupEvent>> = _popupList

    fun fetchPopupComingEvents() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.popupComingApi.getPopupComingEvent("all") // API에 맞게 수정
                _popupList.value = response
            } catch (e: Exception) {
                // 에러 처리
                Log.e("PopupComingViewModel", "팝업 이벤트 불러오기 실패", e)
            }
        }
    }

    fun fetchPopupComingEventsOnce() {
        if (isLoaded) return
        isLoaded = true
        fetchPopupComingEvents()
    }
}