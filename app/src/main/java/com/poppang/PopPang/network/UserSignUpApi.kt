package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserSignUpApi {
    @POST(BuildConfig.V2_KAKAO_SIGNUP_PATH)
    suspend fun signUpWithKakao(@Body request: UserSignUpRequest): Response<ApiResponse<AuthResponse>>
    @POST(BuildConfig.V2_GOOGLE_SIGNUP_PATH)
    suspend fun signUpWithGoogle(@Body request: UserSignUpRequest): Response<ApiResponse<AuthResponse>>
}
