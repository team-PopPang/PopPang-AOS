package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.ViewCountResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ViewCountApi{
    @GET(BuildConfig.POPUP_SELECT_API)
    suspend fun getTotalViewCount(@Path("userUuid") userUuid: String,@Path("popupUuid") popupUuid: String): ViewCountResponse
}

interface ViewCountIncrementApi{
    @POST(BuildConfig.VIEW_COUNT_API)
    suspend fun incrementViewCount(@Path("popupUuid") popupUuid: String)
}