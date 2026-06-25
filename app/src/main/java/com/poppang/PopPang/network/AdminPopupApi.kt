package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path

interface AdminPopupApi {
    @PATCH(BuildConfig.POPUP_DEACTIVATE_API)
    suspend fun deactivatePopup(
        @Path("popupUuid") popupUuid: String,
        @Header("Authorization") authorization: String
    ): Response<Unit>
}
