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

    fun checkNickname() {
        if (nickname.isBlank()) {
            checkResult = "닉네임을 입력해주세요."
            isError = true
            isSuccess = false
            return
        }
        viewModelScope.launch {
            try {
                val response = duplicateNicknameApi.checkNickname(nickname)
                if (response.isSuccessful && response.body() != null) {
                    val isDuplicated = response.body()!!.isDuplicated
                    if (isDuplicated) {
                        checkResult = "이미 사용 중인 닉네임입니다"
                        isError = true
                        isSuccess = false
                    } else {
                        checkResult = "사용 가능한 닉네임입니다"
                        isError = false
                        isSuccess = true
                    }
                } else {
                    checkResult = "서버 오류"
                    isError = true
                    isSuccess = false
                }
            } catch (e: Exception) {
                checkResult = "네트워크 오류"
                isError = true
                isSuccess = false
            }
        }
    }
}