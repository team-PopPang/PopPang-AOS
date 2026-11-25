package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.CategoryItem
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Query

interface MapPopupfilterApi {
    @GET(BuildConfig.V2_MAP_CATEGORIES_PATH)
    suspend fun getMapCategories(): List<CategoryItem>

    @GET(BuildConfig.V2_MAP_FILTER_PATH)
    suspend fun getMapPopupfilter(
        @Query("region") region: String,
        @Query("district") district: String,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("mapSortStandard") mapSortStandard: String,
        @Query("recommendIds") recommendIds: List<Long> = emptyList(),
        @Query("q") query: String? = null,
    ): List<PopupEvent>
}
