package com.pappang.poppang_aos.network

import com.pappang.poppang_aos.model.NicknameCheckResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import com.pappang.poppang_aos.BuildConfig

interface DuplicateNicknameApi {
    @GET(BuildConfig.DUPLICATE_API)
    suspend fun checkNickname(@Query("nickname") nickname: String): Response<NicknameCheckResponse>
}