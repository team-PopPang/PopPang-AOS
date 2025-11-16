package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.FcmTokenRequest
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FcmTokenSendState {
    object Idle : FcmTokenSendState()
    object Success : FcmTokenSendState()
    data class Error(val message: String) : FcmTokenSendState()
}

class FcmTokenViewModel : ViewModel() {
    private val _sendState = MutableStateFlow<FcmTokenSendState>(FcmTokenSendState.Idle)
    val sendState: StateFlow<FcmTokenSendState> = _sendState

    fun sendFcmToken(userUuid: String, fcmToken: String) {
        _sendState.value = FcmTokenSendState.Idle
        viewModelScope.launch {
            try {
                RetrofitInstance.fcmTokenApi.registerToken(
                    userUuid = userUuid,
                    body = FcmTokenRequest(fcmToken)
                )
                _sendState.value = FcmTokenSendState.Success
            } catch (e: Exception) {
                _sendState.value = FcmTokenSendState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
