package com.poppang.PopPang.model

data class ApiResponse<T>(
    val success: Boolean,
    val code: Int,
    val message: String?,
    val data: T?,
) {
    fun requireData(): T {
        check(success && code == 0) { message ?: "요청에 실패했습니다." }
        return checkNotNull(data) { "서버 응답에 필요한 정보가 없습니다." }
    }
}

data class ApiError(val code: Int?, val message: String?)
