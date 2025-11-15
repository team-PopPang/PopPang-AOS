package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.LoginResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserDataApi {
    @GET(BuildConfig.USER_API)
    suspend fun getUserData(
        @Path("userUuid") userUuid: String
    ): Response<LoginResponse>
}