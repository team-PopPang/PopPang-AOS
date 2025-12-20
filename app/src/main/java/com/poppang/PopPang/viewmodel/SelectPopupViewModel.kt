package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SelectPopupViewModel: ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())

    val selectpopupList: StateFlow<List<PopupEvent>> = _popupList

    fun SelectPopupEvents(userUuid: String, popupUuid: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.selectPopupApi.getSelectPopup(userUuid, popupUuid,"all") // API에 맞게 수정
                _popupList.value = listOf(response)
                Log.e("SelectPopupViewModel", "팝업 이벤트 불러오기 성공: ${response}")
            } catch (e: Exception) {
                // 에러 처리
                Log.e("SelectPopupViewModel", "팝업 이벤트 불러오기 실패", e)
                Log.e("SelectPopupViewModel", "팝업 이벤트 불러오기 실패: $popupUuid" )
            }
        }
    }
}