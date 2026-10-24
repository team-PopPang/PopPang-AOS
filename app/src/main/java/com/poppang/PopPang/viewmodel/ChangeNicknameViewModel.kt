package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.ChangeNicknameRequest
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.apiErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ChangeNicknameViewModel : ViewModel() {
    var isUpdating by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun changeNickname(userUuid: String, nickname: String, onResult: (Boolean) -> Unit) {
        if (isUpdating) return
        isUpdating = true
        errorMessage = null
        viewModelScope.launch {
            try {
                RetrofitInstance.changeNicknameApi.changeNickname(ChangeNicknameRequest(nickname.trim()))
                onResult(true)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val fallback = "닉네임을 변경하지 못했습니다. 다시 시도해 주세요."
                errorMessage = (error as? HttpException)?.response()?.let { apiErrorMessage(it, fallback) } ?: fallback
                onResult(false)
            } finally {
                isUpdating = false
            }
        }
    }
}
