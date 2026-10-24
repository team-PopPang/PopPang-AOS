package com.poppang.PopPang.network

import com.poppang.PopPang.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/kakao/mobile/login")
    suspend fun kakaoLogin(@Body request: KakaoLoginRequest): Response<ApiResponse<AuthResponse>>
    @POST("auth/google/mobile/login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<ApiResponse<AuthResponse>>
    @POST("auth/refresh")
    fun refresh(@Body request: RefreshRequest): Call<ApiResponse<TokenResponse>>
}

interface LogoutApi {
    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}
