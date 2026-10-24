package com.poppang.PopPang.network

import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Path

interface PopupApi {
    @GET("user/popups")
    suspend fun getPopupEvent(): List<PopupEvent>
}
interface PopupProgressApi {
    @GET("user/popups/inProgress")
    suspend fun getPopupProgressEvent(): List<PopupEvent>
}
interface PopupComingApi {
    @GET("user/popups/upcoming")
    suspend fun getPopupComingEvent(): List<PopupEvent>
}
interface PopuprelatedApi {
    @GET("user/popups/{popupUuid}/related")
    suspend fun getPopupRelatedEvent(@Path("popupUuid") popupUuid: String): List<PopupEvent>
}
interface SelectPopupApi {
    @GET("user/popups/{popupUuid}")
    suspend fun getSelectPopup(@Path("popupUuid") popupUuid: String): PopupEvent
}
