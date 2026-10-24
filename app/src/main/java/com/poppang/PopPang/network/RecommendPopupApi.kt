package com.poppang.PopPang.network

import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET

interface RecommendPopupApi {
    @GET("user/popups/recommend")
    suspend fun getRecommendPopups(): List<PopupEvent>
}
