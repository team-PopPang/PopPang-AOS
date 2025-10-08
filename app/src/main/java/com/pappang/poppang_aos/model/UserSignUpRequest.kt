package com.pappang.poppang_aos.model

data class UserSignUpRequest(
    val uid: String?,
    val provider: String?,
    val email: String?, // String?으로 변경
    val nickname: String?,
    val role: String?,
    val isAlerted: Boolean?,
    val fcmToken: String?,
    val keywordList: List<String>,
    val recommendList: List<Long>
)
