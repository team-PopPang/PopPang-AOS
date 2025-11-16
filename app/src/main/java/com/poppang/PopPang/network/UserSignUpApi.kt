package com.poppang.PopPang.network

import com.poppang.PopPang.model.UserSignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.poppang.PopPang.BuildConfig

interface UserSignUpApi {
    @POST(BuildConfig.SIGNUP_API_KAKAO)
    suspend fun signUpWithKakao(@Body request: UserSignUpRequest): Response<Unit>

    @POST(BuildConfig.SIGNUP_API_GOOGLE)
    suspend fun signUpWithGoogle(@Body request: UserSignUpRequest): Response<Unit>
}
