package com.pappang.poppang_aos.network

import com.pappang.poppang_aos.BuildConfig
import com.pappang.poppang_aos.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Query

interface PopupApi{
    @GET(BuildConfig.POPUP_API)
    suspend fun getPopupEvent(@Query("eventType") eventType: String): List<PopupEvent>
}

interface PopupComingApi{
    @GET(BuildConfig.POPUP_COMING_API)
    suspend fun getPopupComingEvent(@Query("commingType") comingType: String): List<PopupEvent>
}