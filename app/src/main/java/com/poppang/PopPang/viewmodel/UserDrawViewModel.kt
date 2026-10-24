package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.datastore.AuthTokenStore
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.apiErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.Response

class UserDrawViewModel : ViewModel() {
    var isWorking by mutableStateOf(false)
        private set

    fun withdrawUser(onResult: (String?) -> Unit) = endSession(
        request = { RetrofitInstance.userDrawApi.withdrawUser() },
        fallback = "회원탈퇴에 실패했습니다. 다시 시도해 주세요.",
        onResult = onResult,
    )

    fun logout(onResult: (String?) -> Unit) = endSession(
        request = { RetrofitInstance.logoutApi.logout() },
        fallback = "서버 로그아웃에 실패했습니다. 다시 시도해 주세요.",
        onResult = onResult,
    )

    private fun endSession(
        request: suspend () -> Response<Unit>,
        fallback: String,
        onResult: (String?) -> Unit,
    ) {
        if (isWorking) return
        val session = AuthTokenStore.session
        val generation = session.state.value.generation
        isWorking = true
        viewModelScope.launch {
            try {
                val response = request()
                if (response.isSuccessful) {
                    session.clearIfGeneration(generation)
                    onResult(null)
                } else {
                    onResult(apiErrorMessage(response, fallback))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                onResult(fallback)
            } finally {
                isWorking = false
            }
        }
    }
}
