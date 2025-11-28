package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.DeleteAlertPopupRequest
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface AlertPopupApi {

    @GET(BuildConfig.ALERT_POPUP_API)
    suspend fun getAlertPopup(@Path("userUuid")userUuid: String): List<PopupEvent>

    @PATCH(BuildConfig.ALERT_READ_API)
    suspend fun updateAlertReadStatus(@Path("userUuid") userUuid: String,
                                      @Query("popupUuid") popupUuid: String)

    @HTTP(method = "DELETE", path = BuildConfig.ALERT_DELETE_API, hasBody = true)
    suspend fun deleteAlertPopup(@Path("userUuid") userUuid: String,
                                 @Body request: DeleteAlertPopupRequest
    )
}