package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import retrofit2.http.POST
import retrofit2.http.Path

interface ViewCountIncrementApi {
    @POST(BuildConfig.V2_POPUP_VIEW_PATH)
    suspend fun incrementViewCount(@Path("popupUuid") popupUuid: String)
}
