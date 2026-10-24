package com.poppang.PopPang.network

import retrofit2.Response
import retrofit2.http.DELETE

interface UserDrawApi {
    @DELETE("user")
    suspend fun withdrawUser(): Response<Unit>
}
