package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.KeywordSelectResponse
import com.poppang.PopPang.model.NicknameRecommendationResponse
import retrofit2.Response
import retrofit2.http.GET

interface SignupSuggestionApi {
    @GET(BuildConfig.V2_NICKNAME_RECOMMENDATION_PATH)
    suspend fun recommendNickname(): Response<ApiResponse<NicknameRecommendationResponse>>

    @GET(BuildConfig.V2_POPULAR_KEYWORDS_PATH)
    suspend fun popularKeywords(): Response<ApiResponse<List<KeywordSelectResponse>>>
}
