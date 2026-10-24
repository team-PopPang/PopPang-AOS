package com.poppang.PopPang.network

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.DeleteAlertPopupRequest
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.Query

interface AlertPopupApi {
    @GET("user/alert/popups")
    suspend fun getAlertPopup(): ApiResponse<List<PopupEvent>>
    @PATCH("user/alert/read")
    suspend fun updateAlertReadStatus(@Query("popupUuid") popupUuid: String)
    @HTTP(method = "DELETE", path = "user/alert", hasBody = true)
    suspend fun deleteAlertPopup(@Body request: DeleteAlertPopupRequest)
}
