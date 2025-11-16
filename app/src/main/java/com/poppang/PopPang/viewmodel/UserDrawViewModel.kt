package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.launch

class UserDrawViewModel: ViewModel() {
    fun withdrawUser(userUuid: String) {
        viewModelScope.launch {
            try {
                RetrofitInstance.userDrawApi.withdrawUser(userUuid)
            } catch (e: Exception) {
                Log.e("UserDrawViewModel", "Error withdrawing user: ${e.message}")
            }
        }
    }
}