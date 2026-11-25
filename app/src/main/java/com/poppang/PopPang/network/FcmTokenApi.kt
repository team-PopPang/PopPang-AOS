package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.FcmTokenRequest
import retrofit2.http.Body
import retrofit2.http.PUT

interface FcmTokenApi {
    @PUT(BuildConfig.V2_FCM_TOKEN_PATH)
    suspend fun registerToken(@Body body: FcmTokenRequest)
}
