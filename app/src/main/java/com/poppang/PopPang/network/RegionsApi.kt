package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.RegionsResponse
import retrofit2.http.GET

interface RegionsApi {
    @GET(BuildConfig.REGIONS_API)
    suspend fun getRegions(): List<RegionsResponse>
}