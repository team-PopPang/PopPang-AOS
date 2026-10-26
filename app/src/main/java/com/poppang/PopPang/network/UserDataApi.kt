package com.poppang.PopPang.network

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.SignupOsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserDataApi {
    @GET("user")
    suspend fun getUserData(): Response<ApiResponse<LoginResponse>>

    @PATCH("user/signup-os")
    suspend fun updateSignupOs(@Body request: SignupOsRequest)
}
