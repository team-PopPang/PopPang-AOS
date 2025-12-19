package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PopupRelatedViewModel: ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())
    private var isLoaded = false
    val relatedPopupList: StateFlow<List<PopupEvent>> = _popupList

    fun fetchRelatedPopups(userUuid: String, popupUuid: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.popupRelatedApi.getPopupRelatedEvent(userUuid, popupUuid,"all" )
                _popupList.value = response
                Log.e("PopupRelatedViewModel", "Fetched related popups: $response" )
            }
            catch (e: Exception) {
                Log.e("PopupRelatedViewModel", "Error fetching related popups", e)
            }
        }
    }
    fun fetchrelatedpopupEventsOnce(userUuid: String, popupUuid: String) {
        if (isLoaded) return
        isLoaded = true
        fetchRelatedPopups(userUuid, popupUuid)
    }
}