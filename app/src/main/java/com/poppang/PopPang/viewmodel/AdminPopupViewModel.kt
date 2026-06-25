package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminPopupViewModel : ViewModel() {
    private val _deactivatedPopupUuids = MutableStateFlow<Set<String>>(emptySet())
    val deactivatedPopupUuids: StateFlow<Set<String>> =
        _deactivatedPopupUuids.asStateFlow()

    var isDeactivating by mutableStateOf(false)
        private set

    fun deactivatePopup(
        popupUuid: String,
        authorization: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (popupUuid.isBlank() || authorization.isBlank() || isDeactivating) return

        viewModelScope.launch {
            isDeactivating = true
            try {
                val response = RetrofitInstance.adminPopupApi.deactivatePopup(
                    popupUuid = popupUuid,
                    authorization = authorization
                )
                if (response.isSuccessful) {
                    _deactivatedPopupUuids.value =
                        _deactivatedPopupUuids.value + popupUuid
                    onSuccess()
                } else {
                    val message = when (response.code()) {
                        401 -> "관리자 인증이 만료되었습니다. 다시 로그인해 주세요."
                        403 -> "팝업을 비활성화할 관리자 권한이 없습니다."
                        404 -> "비활성화할 팝업을 찾을 수 없습니다."
                        else -> "팝업 비활성화에 실패했습니다. (${response.code()})"
                    }
                    onError(message)
                }
            } catch (e: Exception) {
                onError("네트워크 연결을 확인한 뒤 다시 시도해 주세요.")
            } finally {
                isDeactivating = false
            }
        }
    }
}
