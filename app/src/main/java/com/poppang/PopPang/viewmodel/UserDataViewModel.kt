package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserDataViewModel: ViewModel() {
    private val _userData = MutableStateFlow<LoginResponse?>(null)
    val userData: StateFlow<LoginResponse?> = _userData

    fun fetchUserData(userUuid: String) {
        viewModelScope.launch {
            try {
                delay(300)
                val response = RetrofitInstance.userDataApi.getUserData(userUuid)
                if (response.isSuccessful) {
                    _userData.value = response.body()
                    Log.d("UserDataViewModel", "User data fetched successfully")
                }
            } catch (e: Exception) {
                Log.e("UserDataViewModel", "Error fetching user data", e)
            }
        }
    }
}