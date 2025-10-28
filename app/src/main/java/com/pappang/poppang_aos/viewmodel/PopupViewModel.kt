package com.pappang.poppang_aos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pappang.poppang_aos.model.PopupEvent
import com.pappang.poppang_aos.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PopupViewModel: ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())
    private var isLoaded = false
    val popupList: StateFlow<List<PopupEvent>> = _popupList

    fun fetchPopupEvents() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.popupApi.getPopupEvent("all") // API에 맞게 수정
                _popupList.value = response
            } catch (e: Exception) {
                // 에러 처리
                Log.e("PopupViewModel", "팝업 이벤트 불러오기 실패", e)
            }
        }
    }

    fun fetchPopupEventsOnce() {
        if (isLoaded) return
        isLoaded = true
        fetchPopupEvents()
    }
}