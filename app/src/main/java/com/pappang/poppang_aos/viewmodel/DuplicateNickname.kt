package com.pappang.poppang_aos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class DuplicateNickname : ViewModel() {
    var nickname by mutableStateOf("")
        private set
    var checkResult by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set
    var isSuccess by mutableStateOf(false)
        private set

    fun onNicknameChange(newNickname: String) {
        nickname = newNickname
        checkResult = ""
        isError = false
        isSuccess = false
    }

    fun checkNickname() {
        if (nickname.trim() == "admin") {
            checkResult = "이미 사용 중인 닉네임입니다"
            isError = true
            isSuccess = false
        } else if (nickname.isNotBlank()) {
            checkResult = "사용 가능한 닉네임입니다"
            isError = false
            isSuccess = true
        } else {
            checkResult = ""
            isError = false
            isSuccess = false
        }
    }
}