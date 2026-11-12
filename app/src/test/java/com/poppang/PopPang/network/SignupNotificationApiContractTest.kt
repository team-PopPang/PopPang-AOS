package com.poppang.PopPang.network

import com.google.gson.JsonParser
import com.poppang.PopPang.model.AlertStatusResponse
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignupNotificationApiContractTest {
    @Test fun optInUsesTheAccessTokenIssuedAtSignupAndTheExistingSettingsEndpoint() =
        runBlocking {
            LocalApiServer().use { server ->
                server.respond = { ApiReply(body = """{"success":true,"code":0,"data":{"isAlerted":true}}""") }
                val session =
                    SessionManager(
                        MemorySessionStorage(StoredSession(signupToken = "pending-signup", signupExpiresAt = Long.MAX_VALUE)),
                        refreshTokens = { error("No refresh expected") },
                    )
                val api =
                    Retrofit
                        .Builder()
                        .baseUrl(server.url + "api/v2/")
                        .client(OkHttpClient.Builder().addInterceptor(SessionInterceptor({ session }, false)).build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(AlertApi::class.java)
                session.acceptLogin(loginData(), "GOOGLE")
                assertTrue(api.updateAlertStatus(AlertStatusResponse(true)).requireData().isAlerted)
                val request = server.takeRequest()
                assertEquals("PATCH", request.method)
                assertEquals("/api/v2/user/alert-status", request.path)
                assertEquals("Bearer login-access", request.authorization)
                assertTrue(request.contentType!!.startsWith("application/json"))
                assertEquals(JsonParser.parseString("""{"isAlerted":true}"""), JsonParser.parseString(request.body))
            }
        }
}
