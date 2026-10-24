package com.poppang.PopPang.network

import com.poppang.PopPang.model.AuthResponse
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.SignupStatus
import com.poppang.PopPang.model.TokenResponse

internal class MemorySessionStorage(initial: StoredSession? = null) : SessionStorage {
    @Volatile var value: StoredSession? = initial
    override fun read() = value
    override fun write(session: StoredSession?) { value = session }
}

internal fun storedSession() = StoredSession(
    accessToken = "old-access", refreshToken = "old-refresh", userUuid = "member-1", provider = "KAKAO",
)

internal fun rotatedTokens() = TokenResponse("Bearer", "new-access", "new-refresh", 900, 3600)

internal fun loginData(user: String = "member-2") = AuthResponse(
    signupStatus = SignupStatus.COMPLETED,
    tokenType = "Bearer",
    user = LoginResponse(userUuid = user, provider = "GOOGLE", nickname = "테스트"),
    accessToken = "login-access", refreshToken = "login-refresh",
    accessTokenExpiresIn = 900, refreshTokenExpiresIn = 3600,
    signupToken = null, signupTokenExpiresIn = null,
)
