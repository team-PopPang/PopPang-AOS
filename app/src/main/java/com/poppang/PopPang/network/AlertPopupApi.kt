package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.DeleteAlertPopupRequest
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.Query

interface AlertPopupApi {
    @GET(BuildConfig.V2_ALERT_POPUPS_PATH)
    suspend fun getAlertPopup(): ApiResponse<List<PopupEvent>>
    @PATCH(BuildConfig.V2_ALERT_READ_PATH)
    suspend fun updateAlertReadStatus(@Query("popupUuid") popupUuid: String)
    @HTTP(method = "DELETE", path = BuildConfig.V2_ALERT_DELETE_PATH, hasBody = true)
    suspend fun deleteAlertPopup(@Body request: DeleteAlertPopupRequest)
}
