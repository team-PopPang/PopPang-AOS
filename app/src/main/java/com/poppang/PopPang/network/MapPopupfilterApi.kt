package com.poppang.PopPang.network

import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Query

interface MapPopupfilterApi {
    @GET("user/popups/filtered/map")
    suspend fun getMapPopupfilter(
        @Query("region") region: String,
        @Query("district") district: String,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("mapSortStandard") mapSortStandard: String,
    ): List<PopupEvent>
}
