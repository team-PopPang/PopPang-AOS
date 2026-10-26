package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.SignupOsRequest
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.UserDataApi
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserDataViewModel(
    private val api: UserDataApi = RetrofitInstance.userDataApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel() {
    private val _userData = MutableStateFlow<LoginResponse?>(null)
    val userData: StateFlow<LoginResponse?> = _userData
    // Activity-scoped: survives screen re-entry/rotation, resets on the next app launch.
    private val signupOsAttempted = AtomicBoolean(false)

    fun updateSignupOsOnce(userUuid: String) {
        if (userUuid.isBlank() || !signupOsAttempted.compareAndSet(false, true)) return
        viewModelScope.launch(dispatcher) {
            try {
                api.updateSignupOs(SignupOsRequest())
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // This optional backfill must not block main or retry during this launch.
            }
        }
    }

    fun reset() {
        viewModelScope.coroutineContext.cancelChildren()
        _userData.value = null
    }

    fun fetchUserData(userUuid: String) {
        viewModelScope.launch(dispatcher) {
            try {
                val response = api.getUserData()
                if (response.isSuccessful) {
                    _userData.value = response.body()?.requireData()
                    Log.d("UserDataViewModel", "User data fetched successfully")
                }
            } catch (e: Exception) {
                Log.e("UserDataViewModel", "Error fetching user data", e)
            }
        }
    }
}
