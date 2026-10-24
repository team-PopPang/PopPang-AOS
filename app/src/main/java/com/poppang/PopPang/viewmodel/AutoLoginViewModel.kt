package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.datastore.AuthTokenStore
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.SignupStatus
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.SessionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionRestoreState {
    data object Loading : SessionRestoreState
    data object SignedOut : SessionRestoreState
    data class Restored(val user: LoginResponse) : SessionRestoreState
    data class Failed(val message: String) : SessionRestoreState
}

class AutoLoginViewModel : ViewModel() {
    private val mutableState = MutableStateFlow<SessionRestoreState>(SessionRestoreState.Loading)
    val state = mutableState.asStateFlow()

    fun restoreSession() {
        mutableState.value = SessionRestoreState.Loading
        viewModelScope.launch {
            val session = AuthTokenStore.session
            try {
                if (session.state.value.status == SessionStatus.SIGNING_UP) {
                    val token = session.signupToken()
                    mutableState.value = if (token != null) {
                        SessionRestoreState.Restored(LoginResponse(
                            provider = session.signupProvider(),
                            signupStatus = SignupStatus.PENDING,
                        ))
                    } else SessionRestoreState.SignedOut
                    return@launch
                }
                if (session.requestSession() == null) {
                    mutableState.value = SessionRestoreState.SignedOut
                    return@launch
                }
                val response = RetrofitInstance.userDataApi.getUserData()
                mutableState.value = if (response.isSuccessful) {
                    SessionRestoreState.Restored(checkNotNull(response.body()).requireData())
                } else if (session.requestSession() == null) {
                    SessionRestoreState.SignedOut
                } else {
                    SessionRestoreState.Failed("로그인 정보를 확인하지 못했습니다. 다시 시도해 주세요.")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.value = if (session.requestSession() == null) {
                    SessionRestoreState.SignedOut
                } else {
                    SessionRestoreState.Failed("서버에 연결하지 못했습니다. 다시 시도해 주세요.")
                }
            }
        }
    }

    fun signOut() {
        AuthTokenStore.session.clear()
        mutableState.value = SessionRestoreState.SignedOut
    }
}
