package com.pappang.poppang_aos.model

data class KakaoLoginRequest(
    val access_token: String
)

data class GoogleLoginRequest(
    val id_token: String
)

data class LoginResponse(
    val uid: String?,
    val provider: String?,
    val email: String?,
    val nickname: String?,
    val role: String?,
    val fcmToken: String?,
    val isAlerted: Boolean?
)
