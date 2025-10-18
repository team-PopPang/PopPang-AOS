package com.pappang.poppang_aos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.model.UserSignUpRequest
import com.pappang.poppang_aos.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    fun signUpUser(
        loginResponse: LoginResponse,
        fcmToken: String?,
        nicknameViewModel: DuplicateNickname,
        keywordViewModel: AddKeywordViewModel,
        categoryViewModel: CategoryItemViewModel,
        onResult: (Boolean) -> Unit
    ) {
        val recommendList = categoryViewModel.selectedCategories.map { it.id }
        val request = UserSignUpRequest(
            uid = loginResponse.uid,
            userUuid = loginResponse.userUuid,
            provider = loginResponse.provider,
            email = loginResponse.email,
            nickname = nicknameViewModel.nickname,
            role = loginResponse.role,
            isAlerted = loginResponse.isAlerted,
            fcmToken = fcmToken,
            keywordList = keywordViewModel.keywordList.toList(),
            recommendList = recommendList
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = when (loginResponse.provider) {
                    "KAKAO" -> RetrofitInstance.userSignUpApi.signUpWithKakao(request)
                    "GOOGLE" -> RetrofitInstance.userSignUpApi.signUpWithGoogle(request)
                    else -> null
                }
                if (response == null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(false)
                        Log.d("SignUpProcess", "회원가입 실패: response=null, request=$request")
                    }
                    return@launch
                }
                CoroutineScope(Dispatchers.Main).launch {
                    if (response.isSuccessful) {
                        Log.d("SignUpProcess", "회원가입 성공: request=$request, response=${response.body()}")
                    } else {
                        Log.e("SignUpProcess", "회원가입 실패: request=$request, errorBody=${response.errorBody()?.string()}")
                    }
                    onResult(response.isSuccessful)
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(false)
                    Log.e("SignUpProcess", "회원가입 예외 발생: ${e.message}, request=$request")
                }
            }
        }
    }
}