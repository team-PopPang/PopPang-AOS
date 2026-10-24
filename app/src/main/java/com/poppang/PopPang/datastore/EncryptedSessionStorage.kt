package com.poppang.PopPang.datastore

import android.content.Context
import android.annotation.SuppressLint
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.gson.Gson
import com.poppang.PopPang.network.SessionStorage
import com.poppang.PopPang.network.StoredSession
import java.io.IOException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class EncryptedSessionStorage(context: Context) : SessionStorage {
    private val preferences = context.applicationContext.getSharedPreferences("v2_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun read(): StoredSession? {
        val payload = preferences.getString("payload", null) ?: return null
        return try {
            val parts = payload.split(':', limit = 2)
            require(parts.size == 2)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
            val json = cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)).toString(Charsets.UTF_8)
            gson.fromJson(json, StoredSession::class.java)
        } catch (_: Exception) {
            preferences.edit().remove("payload").apply()
            null
        }
    }

    @SuppressLint("ApplySharedPref") // Rotated tokens must reach disk before waiting requests resume.
    override fun write(session: StoredSession?) {
        val edit = preferences.edit()
        if (session == null) {
            edit.remove("payload")
        } else {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key())
            val ciphertext = cipher.doFinal(gson.toJson(session).toByteArray(Charsets.UTF_8))
            val payload = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(ciphertext, Base64.NO_WRAP)
            edit.putString("payload", payload)
        }
        if (!edit.commit()) throw IOException("로그인 정보를 저장하지 못했습니다.")
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
        }.generateKey()
    }

    private companion object { const val KEY_ALIAS = "poppang.v2.session" }
}
