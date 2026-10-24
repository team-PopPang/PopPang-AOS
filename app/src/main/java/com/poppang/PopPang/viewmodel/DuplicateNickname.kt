package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.launch

class DuplicateNickname : ViewModel() {
    var nickname by mutableStateOf("")
        private set
    var checkResult by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set
    var isSuccess by mutableStateOf(false)
        private set

    private val duplicateNicknameApi = RetrofitInstance.duplicateNicknameApi

    fun onNicknameChange(newNickname: String) {
        nickname = newNickname
        checkResult = ""
        isError = false
        isSuccess = false
    }

    fun checkNickname(forSignup: Boolean = false) {
        if (nickname.isBlank()) {
            checkResult = "닉네임을 입력해주세요."
            isError = true
            isSuccess = false
            return
        }
        val checkedNickname = nickname.trim()
        viewModelScope.launch {
            try {
                val api = if (forSignup) {
                    RetrofitInstance.signupNicknameApi
                } else {
                    duplicateNicknameApi
                }
                val response = api.checkNickname(checkedNickname)
                check(response.isSuccessful) { "닉네임 확인에 실패했습니다." }
                val isDuplicated = checkNotNull(response.body()).requireData().isDuplicated
                if (nickname.trim() != checkedNickname) return@launch
                run {
                    if (isDuplicated) {
                        checkResult = "이미 사용 중인 닉네임입니다"
                        isError = true
                        isSuccess = false
                    } else {
                        checkResult = "사용 가능한 닉네임입니다"
                        isError = false
                        isSuccess = true
                    }
                }
            } catch (e: Exception) {
                if (nickname.trim() != checkedNickname) return@launch
                checkResult = "네트워크 오류"
                isError = true
                isSuccess = false
            }
        }
    }
}
