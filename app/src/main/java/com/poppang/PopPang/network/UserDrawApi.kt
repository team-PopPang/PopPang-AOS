package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import retrofit2.http.DELETE
import retrofit2.http.Path

interface UserDrawApi {
    @DELETE(BuildConfig.USER_WITHDRAW_API)
    suspend fun withdrawUser(@Path("userUuid") userUuid: String)
}