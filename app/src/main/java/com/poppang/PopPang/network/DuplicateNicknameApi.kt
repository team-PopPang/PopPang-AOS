package com.poppang.PopPang.network

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.NicknameCheckResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DuplicateNicknameApi {
    @GET("user/nickname/duplicated")
    suspend fun checkNickname(@Query("nickname") nickname: String): Response<ApiResponse<NicknameCheckResponse>>
}
