package com.poppang.PopPang.network

import com.poppang.PopPang.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserSignUpApi {
    @POST("auth/kakao/signup")
    suspend fun signUpWithKakao(@Body request: UserSignUpRequest): Response<ApiResponse<AuthResponse>>
    @POST("auth/google/signup")
    suspend fun signUpWithGoogle(@Body request: UserSignUpRequest): Response<ApiResponse<AuthResponse>>
}
