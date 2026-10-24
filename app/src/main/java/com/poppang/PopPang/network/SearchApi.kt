package com.poppang.PopPang.network

import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {
    @GET("user/popups/search")
    suspend fun search(@Query("q") q: String): List<PopupEvent>
}
