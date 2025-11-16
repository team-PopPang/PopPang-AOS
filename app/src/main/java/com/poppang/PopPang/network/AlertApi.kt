package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.AlertStatusResponse
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

interface AlertApi {
    @PATCH(BuildConfig.ALERT_API)
    suspend fun updateAlertStatus(
        @Path("userUuid") userUuid: String,
        @Body body: AlertStatusResponse)
}