package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapPopupfilterApi {
    @GET(BuildConfig.MAP_POPUP_API)
    suspend fun getMapPopupfilter(
        @Path("userUuid") userUuid: String,
        @Query("region") region: String,
        @Query("district") district: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("mapSortStandard") mapSortStandard: String
    ): List<PopupEvent>
}