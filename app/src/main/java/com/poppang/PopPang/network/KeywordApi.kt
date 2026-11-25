package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.KeywordDeleteRequest
import com.poppang.PopPang.model.KeywordInsertRequest
import com.poppang.PopPang.model.KeywordSelectResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST

interface KeywordApi {
    @GET(BuildConfig.V2_ALERT_KEYWORDS_PATH)
    suspend fun getKeyword(): ApiResponse<List<KeywordSelectResponse>>
    @POST(BuildConfig.V2_ALERT_KEYWORDS_PATH)
    suspend fun insertKeyword(@Body request: KeywordInsertRequest)
    @HTTP(method = "DELETE", path = BuildConfig.V2_ALERT_KEYWORDS_PATH, hasBody = true)
    suspend fun deleteKeyword(@Body request: KeywordDeleteRequest)
}
