package com.poppang.PopPang.network

import com.poppang.PopPang.model.ViewCountResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ViewCountApi {
    @GET("popup/{popupUuid}/total-view-count")
    suspend fun getTotalViewCount(@Path("popupUuid") popupUuid: String): ViewCountResponse
}
interface ViewCountIncrementApi {
    @POST("popup/{popupUuid}/view")
    suspend fun incrementViewCount(@Path("popupUuid") popupUuid: String)
}
