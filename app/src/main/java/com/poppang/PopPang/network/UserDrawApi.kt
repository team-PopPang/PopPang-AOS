package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import retrofit2.Response
import retrofit2.http.DELETE

interface UserDrawApi {
    @DELETE(BuildConfig.V2_USER_PATH)
    suspend fun withdrawUser(): Response<Unit>
}
