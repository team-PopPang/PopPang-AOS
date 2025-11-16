package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.ChangeNicknameRequest
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.launch

class ChangeNicknameViewModel : ViewModel() {
    fun changeNickname(userUuid: String, nickname: String) {
        viewModelScope.launch {
            Log.d("ChangeNicknameViewModel", "Attempting to change nickname to $nickname for userUuid: $userUuid")
            try{
                RetrofitInstance.changeNicknameApi.changeNickname(
                    userUuid,
                    ChangeNicknameRequest(nickname)
                )
                Log.d("ChangeNicknameViewModel", "changeNickname API call successful")
            }
            catch (e: Exception) {
                Log.e("ChangeNicknameViewModel", "Error changing nickname", e)
            }
        }
    }
}