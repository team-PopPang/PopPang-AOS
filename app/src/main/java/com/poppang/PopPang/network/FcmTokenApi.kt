package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.FcmTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface FcmTokenApi {
    @POST(BuildConfig.FCM_API)
    suspend fun registerToken(
        @Path("userUuid") userUuid: String,
        @Body body: FcmTokenRequest
    )
}