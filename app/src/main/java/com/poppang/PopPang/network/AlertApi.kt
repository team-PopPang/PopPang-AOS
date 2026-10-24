package com.poppang.PopPang.network

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.AlertStatusResponse
import retrofit2.http.Body
import retrofit2.http.PATCH

interface AlertApi {
    @PATCH("user/alert-status")
    suspend fun updateAlertStatus(@Body body: AlertStatusResponse): ApiResponse<AlertStatusResponse>
}
