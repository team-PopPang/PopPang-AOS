package com.pappang.poppang_aos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.pappang.poppang_aos.model.KakaoLoginRequest
import com.pappang.poppang_aos.model.GoogleLoginRequest
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.model.AutoLoginRequest
import com.pappang.poppang_aos.BuildConfig

interface AuthApi {
    @POST(BuildConfig.AUTH_API_KAKAO)
    suspend fun kakaoLogin(@Body request: KakaoLoginRequest): Response<LoginResponse>

    @POST(BuildConfig.AUTH_API_GOOGLE)
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<LoginResponse>

    @POST(BuildConfig.AUTH_API_AUTOLOGIN)
    suspend fun autoLogin(@Body request: AutoLoginRequest): Response<LoginResponse>
}
