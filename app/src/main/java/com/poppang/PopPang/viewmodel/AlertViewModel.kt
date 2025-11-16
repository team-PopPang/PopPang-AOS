package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.AlertStatusResponse
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlertViewModel: ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())
    val alertPopupList: StateFlow<List<PopupEvent>> = _popupList
    var alertedState by mutableStateOf(false)
        private set
    fun loadAlertStatus(isAlerted: Boolean) {
        alertedState = isAlerted
    }

    fun AlertStatus(userUuid: String, isAlerted: Boolean) {
        viewModelScope.launch {
            try {
                RetrofitInstance.alertApi.updateAlertStatus(
                    userUuid = userUuid,
                    body = AlertStatusResponse(isAlerted)
                )
                alertedState = isAlerted
                Log.e("AlertViewModel", "Alert status updated successfully to $isAlerted")
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Error updating alert status: ${e.message}")
            }
        }
    }
    fun fetchalertpopup(userUuid: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.alertPopupApi.getAlertPopup(userUuid = userUuid)
                Log.e("AlertViewModel", "Fetched alert status successfully")
                _popupList.value = response
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Error fetching alert status: ${e.message}")
            }
        }
    }
    fun updateAlertReadStatus(userUuid: String, popupUuid: String) {
        viewModelScope.launch {
            try {
                RetrofitInstance.alertPopupApi.updateAlertReadStatus(
                    userUuid = userUuid,
                    popupUuid = popupUuid
                )
                Log.e("AlertViewModel", "Alert read status updated successfully for popupUuid: $popupUuid")
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Error updating alert read status: ${e.message}")
            }
        }
    }
}