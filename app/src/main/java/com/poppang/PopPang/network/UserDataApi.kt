package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.SignupOsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserDataApi {
    @GET(BuildConfig.V2_USER_PATH)
    suspend fun getUserData(): Response<ApiResponse<LoginResponse>>

    @PATCH(BuildConfig.V2_SIGNUP_OS_PATH)
    suspend fun updateSignupOs(@Body request: SignupOsRequest)
}
