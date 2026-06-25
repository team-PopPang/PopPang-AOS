package com.poppang.PopPang.model

import com.google.gson.annotations.SerializedName

data class KakaoLoginRequest(
    val access_token: String
)

data class GoogleLoginRequest(
    val id_token: String
)

data class LoginResponse(
    val uid: String?,
    val userUuid: String?,
    val provider: String?,
    val email: String?,
    val nickname: String?,
    val role: String?,
    val fcmToken: String?,
    val isAlerted: Boolean?,
    @SerializedName(
        value = "accessToken",
        alternate = ["access_token", "token", "jwt"]
    )
    val accessToken: String? = null
)
