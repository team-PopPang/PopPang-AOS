package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET

interface RecommendPopupApi {
    @GET(BuildConfig.V2_HOME_RECOMMENDATIONS_PATH)
    suspend fun getRecommendPopups(): List<PopupEvent>
}
