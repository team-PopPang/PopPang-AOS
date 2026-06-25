package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.AdminPopupSubmissionDetail
import com.poppang.PopPang.model.AdminPopupSubmissionListItem
import com.poppang.PopPang.model.AdminPopupSubmissionUpdateRequest
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.launch

class AdminSubmissionViewModel : ViewModel() {
    var allSubmissions by mutableStateOf<List<AdminPopupSubmissionListItem>>(emptyList())
        private set

    var submissions by mutableStateOf<List<AdminPopupSubmissionListItem>>(emptyList())
        private set

    var selectedDetail by mutableStateOf<AdminPopupSubmissionDetail?>(null)
        private set

    var selectedFilter by mutableStateOf(AdminSubmissionFilter.ALL)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isDetailLoading by mutableStateOf(false)
        private set

    var isUpdating by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadSubmissions(
        adminUuid: String,
        filter: AdminSubmissionFilter = selectedFilter
    ) {
        if (adminUuid.isBlank() || isLoading) return

        viewModelScope.launch {
            selectedFilter = filter
            isLoading = true
            errorMessage = null
            try {
                val result = RetrofitInstance.adminSubmissionApi.getAdminPopupSubmissions(
                    adminUuid = adminUuid,
                    status = filter.apiValue
                )
                submissions = result
                if (filter == AdminSubmissionFilter.ALL) {
                    allSubmissions = result
                }
            } catch (e: Exception) {
                errorMessage = "제보 목록을 불러오지 못했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    fun refresh(adminUuid: String) {
        if (adminUuid.isBlank() || isLoading) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val allResult = RetrofitInstance.adminSubmissionApi.getAdminPopupSubmissions(
                    adminUuid = adminUuid,
                    status = AdminSubmissionFilter.ALL.apiValue
                )
                allSubmissions = allResult
                submissions = if (selectedFilter == AdminSubmissionFilter.ALL) {
                    allResult
                } else {
                    RetrofitInstance.adminSubmissionApi.getAdminPopupSubmissions(
                        adminUuid = adminUuid,
                        status = selectedFilter.apiValue
                    )
                }
            } catch (e: Exception) {
                errorMessage = "제보 목록을 불러오지 못했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    fun loadDetail(
        adminUuid: String,
        popupUuid: String?,
        popupSubmissionId: Long?
    ) {
        if (
            adminUuid.isBlank() ||
            (popupUuid.isNullOrBlank() && popupSubmissionId == null) ||
            isDetailLoading
        ) {
            return
        }

        viewModelScope.launch {
            isDetailLoading = true
            errorMessage = null
            selectedDetail = null
            try {
                selectedDetail = if (!popupUuid.isNullOrBlank()) {
                    RetrofitInstance.adminSubmissionApi
                        .getAdminPopupSubmissionDetail(popupUuid, adminUuid)
                } else {
                    RetrofitInstance.adminSubmissionApi
                        .getAdminPopupSubmissionDetailById(
                            popupSubmissionId = requireNotNull(popupSubmissionId),
                            adminUuid = adminUuid
                        )
                }
            } catch (e: Exception) {
                errorMessage = "제보 상세 정보를 불러오지 못했습니다."
            } finally {
                isDetailLoading = false
            }
        }
    }

    fun clearDetail() {
        selectedDetail = null
        errorMessage = null
    }

    fun updateSubmission(
        adminUuid: String,
        popupUuid: String?,
        popupSubmissionId: Long?,
        request: AdminPopupSubmissionUpdateRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (
            adminUuid.isBlank() ||
            (popupUuid.isNullOrBlank() && popupSubmissionId == null) ||
            isUpdating
        ) {
            return
        }

        viewModelScope.launch {
            isUpdating = true
            try {
                val response = if (!popupUuid.isNullOrBlank()) {
                    RetrofitInstance.adminSubmissionApi.updateAdminPopupSubmission(
                        popupUuid = popupUuid,
                        adminUuid = adminUuid,
                        request = request
                    )
                } else {
                    RetrofitInstance.adminSubmissionApi.updateAdminPopupSubmissionById(
                        popupSubmissionId = requireNotNull(popupSubmissionId),
                        adminUuid = adminUuid,
                        request = request
                    )
                }
                if (response.isSuccessful) {
                    clearDetail()
                    refresh(adminUuid)
                    onSuccess()
                } else {
                    onError("상태 변경에 실패했습니다. (${response.code()})")
                }
            } catch (e: Exception) {
                onError("네트워크 연결을 확인한 뒤 다시 시도해 주세요.")
            } finally {
                isUpdating = false
            }
        }
    }

    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_REJECTED = "REJECTED"
    }
}

enum class AdminSubmissionFilter(
    val label: String,
    val apiValue: String
) {
    ALL("전체", "전체"),
    PENDING("대기", "대기"),
    APPROVED("승인", "승인"),
    REJECTED("반려", "반려")
}
