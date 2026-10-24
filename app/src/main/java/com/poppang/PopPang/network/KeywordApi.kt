package com.poppang.PopPang.network

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.KeywordDeleteRequest
import com.poppang.PopPang.model.KeywordInsertRequest
import com.poppang.PopPang.model.KeywordSelectResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST

interface KeywordApi {
    @GET("alert-keyword")
    suspend fun getKeyword(): ApiResponse<List<KeywordSelectResponse>>
    @POST("alert-keyword")
    suspend fun insertKeyword(@Body request: KeywordInsertRequest)
    @HTTP(method = "DELETE", path = "alert-keyword", hasBody = true)
    suspend fun deleteKeyword(@Body request: KeywordDeleteRequest)
}
