package com.pappang.poppang_aos.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pappang.poppang_aos.model.AutoLoginRequest
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutoLoginViewModel : ViewModel() {
    private val _loginResponse = MutableStateFlow<LoginResponse?>(null)
    val loginResponse: StateFlow<LoginResponse?> = _loginResponse

    private val _loading = MutableStateFlow(false)

    fun getuserUuid(context: Context): String? {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userUuid = prefs.getString("userUuid", null)
        return userUuid
    }

    fun autoLogin(context: Context) {
        val userUuid = getuserUuid(context)
        if (userUuid.isNullOrBlank()) return
        _loading.value = true
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                try {
                    RetrofitInstance.authApi.autoLogin(AutoLoginRequest(userUuid))
                } catch (e: Exception) {
                    null
                }
            }
            if (response != null && response.isSuccessful) {
                _loginResponse.value = response.body()
            } else {
                _loginResponse.value = null
            }
            _loading.value = false
        }
    }
}
