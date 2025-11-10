package com.pappang.poppang_aos.network

import com.pappang.poppang_aos.BuildConfig
import com.pappang.poppang_aos.model.ViewCountResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ViewCountApi{
    @POST(BuildConfig.VIEW_COUNT_API)
    suspend fun incrementViewCount(@Path("popupUuid") popupUuid: String)

    @GET(BuildConfig.VIEW_COUNT_TOTAL_API)
    suspend fun getTotalViewCount(@Path("popupUuid") popupUuid: String): ViewCountResponse
}