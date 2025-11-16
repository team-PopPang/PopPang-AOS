package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.AlertStatusResponse
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.launch

class AlertViewModel: ViewModel() {
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
}