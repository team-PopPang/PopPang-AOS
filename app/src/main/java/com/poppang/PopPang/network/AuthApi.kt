package com.poppang.PopPang.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.poppang.PopPang.model.KakaoLoginRequest
import com.poppang.PopPang.model.GoogleLoginRequest
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.AutoLoginRequest
import com.poppang.PopPang.BuildConfig

interface AuthApi {
    @POST(BuildConfig.AUTH_API_KAKAO)
    suspend fun kakaoLogin(@Body request: KakaoLoginRequest): Response<LoginResponse>

    @POST(BuildConfig.AUTH_API_GOOGLE)
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<LoginResponse>

    @POST(BuildConfig.AUTH_API_AUTOLOGIN)
    suspend fun autoLogin(@Body request: AutoLoginRequest): Response<LoginResponse>
}
