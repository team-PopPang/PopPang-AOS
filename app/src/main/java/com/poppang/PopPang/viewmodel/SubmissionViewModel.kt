package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupSubmissionRequest
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.launch

class SubmissionViewModel : ViewModel() {
    var isSubmitting by mutableStateOf(false)
        private set

    fun submitPopup(
        request: PopupSubmissionRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (isSubmitting) return

        viewModelScope.launch {
            isSubmitting = true
            try {
                val response = RetrofitInstance.submissionApi.createPopupSubmission(request)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("제보 등록에 실패했습니다. (${response.code()})")
                }
            } catch (e: Exception) {
                onError("네트워크 연결을 확인한 뒤 다시 시도해 주세요.")
            } finally {
                isSubmitting = false
            }
        }
    }
}
