package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST(BuildConfig.V2_KAKAO_LOGIN_PATH)
    suspend fun kakaoLogin(@Body request: KakaoLoginRequest): Response<ApiResponse<AuthResponse>>
    @POST(BuildConfig.V2_GOOGLE_LOGIN_PATH)
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<ApiResponse<AuthResponse>>
    @POST(BuildConfig.V2_AUTH_REFRESH_PATH)
    fun refresh(@Body request: RefreshRequest): Call<ApiResponse<TokenResponse>>
}

interface LogoutApi {
    @POST(BuildConfig.V2_LOGOUT_PATH)
    suspend fun logout(): Response<Unit>
}
