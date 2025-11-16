package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecommendPopupApi {
    @GET(BuildConfig.RECOMMEND_POPUP_API)
    suspend fun getRecommendPopups(
        @Path("userUuid") userUuid: String,
        @Query("recommendpopup") recommendpopup: String
    ): List<PopupEvent>
}