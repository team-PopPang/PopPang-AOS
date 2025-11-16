package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecommendPopupViewModel : ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())
    private var isLoaded = false
    val recommedPopupList: StateFlow<List<PopupEvent>> = _popupList

    fun fetchRecommendPopups(userUuid: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.recommendPopupApi.getRecommendPopups(userUuid, "all")
                _popupList.value = response
                Log.e("RecommendPopupViewModel", "Fetched recommended popups: ${response.size}" )
            }
            catch (e: Exception) {
                Log.e("RecommendPopupViewModel", "Error fetching recommended popups: ${e.message}")
            }
        }
    }
    fun fetchrecommendpopupEventsOnce(userUuid: String) {
        if (isLoaded) return
        isLoaded = true
        fetchRecommendPopups(userUuid)
    }
}