package com.poppang.PopPang.model

data class KakaoLoginRequest(
    val access_token: String
)

data class GoogleLoginRequest(
    val id_token: String
)

data class LoginResponse(
    val userUuid: String? = null,
    val provider: String? = null,
    val email: String? = null,
    val nickname: String? = null,
    val role: String? = null,
    val isAlerted: Boolean? = null,
    val signupStatus: SignupStatus = SignupStatus.COMPLETED,
)
