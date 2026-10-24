package com.poppang.PopPang.model

enum class SignupStatus { PENDING, COMPLETED }

data class AuthResponse(
    val signupStatus: SignupStatus,
    val tokenType: String?,
    val user: LoginResponse?,
    val accessToken: String?,
    val refreshToken: String?,
    val accessTokenExpiresIn: Long?,
    val refreshTokenExpiresIn: Long?,
    val signupToken: String?,
    val signupTokenExpiresIn: Long?,
)

data class TokenResponse(
    val tokenType: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
)

data class RefreshRequest(val refreshToken: String)
