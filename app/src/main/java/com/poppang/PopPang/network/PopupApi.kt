package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Path

interface PopupApi {
    @GET(BuildConfig.V2_POPUPS_PATH)
    suspend fun getPopupEvent(): List<PopupEvent>
}
interface PopupComingApi {
    @GET(BuildConfig.V2_UPCOMING_POPUPS_PATH)
    suspend fun getPopupComingEvent(): List<PopupEvent>
}
interface PopuprelatedApi {
    @GET(BuildConfig.V2_RELATED_POPUPS_PATH)
    suspend fun getPopupRelatedEvent(@Path("popupUuid") popupUuid: String): List<PopupEvent>
}
interface SelectPopupApi {
    @GET(BuildConfig.V2_POPUP_DETAIL_PATH)
    suspend fun getSelectPopup(@Path("popupUuid") popupUuid: String): PopupEvent
}
