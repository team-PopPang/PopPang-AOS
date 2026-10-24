package com.poppang.PopPang.network

import com.poppang.PopPang.model.ChangeNicknameRequest
import retrofit2.http.Body
import retrofit2.http.PATCH

interface ChangeNicknameApi {
    @PATCH("user")
    suspend fun changeNickname(@Body request: ChangeNicknameRequest)
}
