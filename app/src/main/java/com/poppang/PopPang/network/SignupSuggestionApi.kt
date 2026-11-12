package com.poppang.PopPang.network

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.KeywordSelectResponse
import com.poppang.PopPang.model.NicknameRecommendationResponse
import retrofit2.Response
import retrofit2.http.GET

interface SignupSuggestionApi {
    @GET("user/nickname/recommendation")
    suspend fun recommendNickname(): Response<ApiResponse<NicknameRecommendationResponse>>

    @GET("alert-keyword/popular")
    suspend fun popularKeywords(): Response<ApiResponse<List<KeywordSelectResponse>>>
}
