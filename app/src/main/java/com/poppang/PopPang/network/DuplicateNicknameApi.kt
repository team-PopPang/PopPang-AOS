package com.poppang.PopPang.network

import com.poppang.PopPang.model.NicknameCheckResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import com.poppang.PopPang.BuildConfig

interface DuplicateNicknameApi {
    @GET(BuildConfig.DUPLICATE_API)
    suspend fun checkNickname(@Query("nickname") nickname: String): Response<NicknameCheckResponse>
}