package com.pappang.poppang_aos.network

import com.pappang.poppang_aos.BuildConfig
import com.pappang.poppang_aos.model.KeywordDeleteRequest
import com.pappang.poppang_aos.model.KeywordInsertRequest
import com.pappang.poppang_aos.model.KeywordSelectResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface KeywordApi {
    @GET(BuildConfig.KEYWORD_API)
    suspend fun getKeyword(@Query("userUuid") userUuid: String): List<KeywordSelectResponse>

    @POST(BuildConfig.KEYWORD_API)
    suspend fun insertKeyword(@Body request: KeywordInsertRequest)

    @HTTP(method = "DELETE", path = BuildConfig.KEYWORD_API, hasBody = true)
    suspend fun deleteKeyword(@Body request: KeywordDeleteRequest)
}