package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.ChangeNicknameRequest
import retrofit2.http.Body
import retrofit2.http.PATCH

interface ChangeNicknameApi {
    @PATCH(BuildConfig.V2_USER_PATH)
    suspend fun changeNickname(@Body request: ChangeNicknameRequest)
}
