package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.ChangeNicknameRequest
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ChangeNicknameApi {
    @PATCH(BuildConfig.USER_API)
    suspend fun changeNickname(
        @Path("userUuid") userUuid: String,
        @Body requset: ChangeNicknameRequest
    )
}