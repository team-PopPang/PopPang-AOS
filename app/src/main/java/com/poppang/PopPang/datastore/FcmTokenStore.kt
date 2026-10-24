package com.poppang.PopPang.datastore

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object FcmTokenStore {
    private val mutableToken = MutableStateFlow<String?>(null)
    val token = mutableToken.asStateFlow()

    fun initialize(context: Context) {
        mutableToken.value = context.getSharedPreferences("fcm", Context.MODE_PRIVATE).getString("fcm_token", null)
    }

    fun update(context: Context, token: String) {
        context.getSharedPreferences("fcm", Context.MODE_PRIVATE).edit().putString("fcm_token", token).apply()
        mutableToken.value = token
    }
}
