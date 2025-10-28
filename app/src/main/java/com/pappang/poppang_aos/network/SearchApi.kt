package com.pappang.poppang_aos.network

import com.pappang.poppang_aos.BuildConfig
import com.pappang.poppang_aos.model.PopupEvent
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi{
    @GET(BuildConfig.SEARCH_API)
    suspend fun search(@Query("q") q: String): List<PopupEvent>
}