package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Query

interface PopupApi {
    @GET(BuildConfig.POPUP_API)
    suspend fun getPopupEvent(@Query("popup") popup: String): List<PopupEvent>
}
interface PopupProgressApi{
    @GET(BuildConfig.POPUP_PROGRESS_API)
    suspend fun getPopupProgressEvent(@Query("progressType") progressType: String): List<PopupEvent>
}

interface PopupComingApi{
    @GET(BuildConfig.POPUP_COMING_API)
    suspend fun getPopupComingEvent(@Query("commingType") comingType: String): List<PopupEvent>
}