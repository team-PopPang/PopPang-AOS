package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Query

interface HomePopupfilterApi {
    @GET(BuildConfig.V2_HOME_FILTER_PATH)
    suspend fun getHomePopupfilter(
        @Query("region") region: String,
        @Query("district") district: String,
        @Query("homeSortStandard") homeSortStandard: String,
    ): List<PopupEvent>
}
