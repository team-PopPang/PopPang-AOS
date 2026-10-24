package com.poppang.PopPang.network

import com.poppang.PopPang.model.FcmTokenRequest
import retrofit2.http.Body
import retrofit2.http.PUT

interface FcmTokenApi {
    @PUT("user/fcm-token")
    suspend fun registerToken(@Body body: FcmTokenRequest)
}
