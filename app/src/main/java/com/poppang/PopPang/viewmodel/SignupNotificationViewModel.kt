package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.AlertStatusResponse
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.network.AlertApi
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignupNotificationViewModel(
    private val api: AlertApi = RetrofitInstance.alertApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel() {
    var isEnabling by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun enable(
        profile: LoginResponse,
        onEnabled: (LoginResponse) -> Unit,
    ) {
        if (isEnabling) return
        isEnabling = true
        errorMessage = null
        viewModelScope.launch(dispatcher) {
            try {
                val result = api.updateAlertStatus(AlertStatusResponse(isAlerted = true)).requireData()
                check(result.isAlerted)
                onEnabled(profile.copy(isAlerted = true))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                errorMessage = "알림 설정을 저장하지 못했어요. 다시 시도하거나 나중에 마이 > 알림 설정에서 켜주세요."
            } finally {
                isEnabling = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
