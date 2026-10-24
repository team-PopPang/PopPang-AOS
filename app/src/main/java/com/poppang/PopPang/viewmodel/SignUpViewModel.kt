package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.datastore.AuthTokenStore
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.SignupStatus
import com.poppang.PopPang.model.UserSignUpRequest
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.apiErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    var isSubmitting by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun signUpUser(
        loginResponse: LoginResponse,
        fcmToken: String?,
        nicknameViewModel: DuplicateNickname,
        keywordViewModel: AddKeywordViewModel,
        categoryViewModel: CategoryItemViewModel,
        onResult: (LoginResponse?) -> Unit,
    ) {
        if (isSubmitting) return
        val provider = loginResponse.provider ?: return
        val request = UserSignUpRequest(
            nickname = nicknameViewModel.nickname.trim(),
            isAlerted = loginResponse.isAlerted ?: false,
            fcmToken = fcmToken,
            alertKeywordList = keywordViewModel.keywordList.toList(),
            recommendList = categoryViewModel.selectedCategories.map { it.id },
            signupOs = "ANDROID",
        )
        isSubmitting = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = when (provider) {
                    "KAKAO" -> RetrofitInstance.userSignUpApi.signUpWithKakao(request)
                    "GOOGLE" -> RetrofitInstance.userSignUpApi.signUpWithGoogle(request)
                    else -> error("지원하지 않는 로그인 방식입니다.")
                }
                check(response.isSuccessful) { apiErrorMessage(response, "회원가입에 실패했습니다.") }
                val data = checkNotNull(response.body()).requireData()
                check(data.signupStatus == SignupStatus.COMPLETED) { "회원가입이 완료되지 않았습니다." }
                onResult(AuthTokenStore.session.acceptLogin(data, provider))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                errorMessage = error.message ?: "회원가입에 실패했습니다. 다시 시도해 주세요."
                onResult(null)
            } finally {
                isSubmitting = false
            }
        }
    }
}
