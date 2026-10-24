package com.poppang.PopPang.network

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.LoginResponse
import retrofit2.Response
import retrofit2.http.GET

interface UserDataApi {
    @GET("user")
    suspend fun getUserData(): Response<ApiResponse<LoginResponse>>
}
