package com.poppang.PopPang.datastore

import android.content.Context
import com.poppang.PopPang.model.LoginResponse
import retrofit2.Response

object AuthTokenStore {
    private const val PREFERENCES_NAME = "auth_prefs"
    private const val ACCESS_TOKEN_KEY = "access_token"

    fun saveFromLoginResponse(
        context: Context,
        response: Response<LoginResponse>
    ) {
        val token = response.body()?.accessToken
            ?: response.headers()["Authorization"]
            ?: response.headers()["Access-Token"]
            ?: response.headers()["access-token"]

        save(context, token)
    }

    fun getAuthorizationHeader(context: Context): String? {
        val token = context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(ACCESS_TOKEN_KEY, null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        return if (token.startsWith("Bearer ", ignoreCase = true)) {
            token
        } else {
            "Bearer $token"
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(ACCESS_TOKEN_KEY)
            .apply()
    }

    private fun save(
        context: Context,
        rawToken: String?
    ) {
        val token = rawToken
            ?.trim()
            ?.removePrefix("Bearer ")
            ?.removePrefix("bearer ")
            ?.takeIf { it.isNotBlank() }
            ?: return

        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(ACCESS_TOKEN_KEY, token)
            .apply()
    }
}
