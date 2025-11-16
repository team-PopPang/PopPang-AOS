package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi{
    @GET(BuildConfig.SEARCH_API)
    suspend fun search(@Query("q") q: String): List<PopupEvent>
}