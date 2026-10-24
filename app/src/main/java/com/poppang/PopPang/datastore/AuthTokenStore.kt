package com.poppang.PopPang.datastore

import android.content.Context
import com.poppang.PopPang.model.RefreshRequest
import com.poppang.PopPang.network.RefreshRejected
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.SessionManager

object AuthTokenStore {
    lateinit var session: SessionManager
        private set

    @Synchronized
    fun initialize(context: Context) {
        if (::session.isInitialized) return
        // V1 UUIDs and old plaintext tokens cannot establish a V2 session.
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().remove("userUuid").apply()
        session = SessionManager(
            EncryptedSessionStorage(context),
            refreshTokens = { refreshToken ->
                val response = RetrofitInstance.authApi.refresh(RefreshRequest(refreshToken)).execute()
                if (!response.isSuccessful) {
                    response.errorBody()?.close()
                    throw RefreshRejected(response.code())
                }
                val body = response.body()
                if (body?.success != true || body.code != 0 || body.data == null) throw RefreshRejected(500)
                body.data
            },
        )
    }

    fun getAuthorizationHeader(context: Context): String? {
        initialize(context)
        return session.requestSession()?.accessToken?.let { "Bearer $it" }
    }

    fun clear(context: Context) {
        initialize(context)
        session.clear()
    }
}
