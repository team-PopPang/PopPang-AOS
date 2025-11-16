package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HomePopuofilterApi {
    @GET(BuildConfig.HOME_POPUP_API)
    suspend fun getHomePopupfilter(
        @Path("userUuid") userUuid: String,
        @Query("region") region: String,
        @Query("district") district: String,
        @Query("homeSortStandard") homeSortStandard: String
    ): List<PopupEvent>

}