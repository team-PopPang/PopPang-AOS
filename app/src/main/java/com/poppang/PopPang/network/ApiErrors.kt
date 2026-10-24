package com.poppang.PopPang.network

import com.google.gson.Gson
import com.poppang.PopPang.model.ApiError
import retrofit2.Response

fun apiErrorMessage(response: Response<*>, fallback: String): String = try {
    val body = response.errorBody()?.string()
    if (body.isNullOrBlank()) fallback else Gson().fromJson(body, ApiError::class.java)?.message ?: fallback
} catch (_: Exception) {
    fallback
}
