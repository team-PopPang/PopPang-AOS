package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.RegionsResponse
import retrofit2.http.GET

interface RegionsApi {
    @GET(BuildConfig.V2_DISTRICTS_PATH)
    suspend fun getRegions(): List<RegionsResponse>
}
