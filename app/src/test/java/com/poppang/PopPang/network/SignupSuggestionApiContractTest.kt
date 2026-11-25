package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.google.gson.Gson
import com.poppang.PopPang.model.ApiError
import com.poppang.PopPang.model.AuthResponse
import com.poppang.PopPang.model.SignupStatus
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignupSuggestionApiContractTest {
    @Test
    fun bothQueriesUseBearerAuthenticationWithoutQueryOrBodyForBothSessionTypes() =
        runBlocking {
            for (signup in listOf(true, false)) {
                LocalApiServer().use { server ->
                    server.respond = { request ->
                        ApiReply(body = if (request.path == server.mobilePath(BuildConfig.V2_NICKNAME_RECOMMENDATION_PATH)) nicknameJson else popularJson)
                    }
                    val session =
                        SessionManager(
                            MemorySessionStorage(if (signup) pendingSession else storedSession()),
                            refreshTokens = { error("These successful GET requests must not refresh") },
                        )
                    val api = api(server, session, signup)
                    assertEquals(
                        "설레는수달",
                        api
                            .recommendNickname()
                            .body()!!
                            .requireData()
                            .nickname,
                    )
                    assertEquals(
                        listOf("소금빵", "산리오"),
                        api
                            .popularKeywords()
                            .body()!!
                            .requireData()
                            .map { it.alertKeyword },
                    )
                    val requests = listOf(server.takeRequest(), server.takeRequest())
                    assertEquals(
                        listOf(server.mobilePath(BuildConfig.V2_NICKNAME_RECOMMENDATION_PATH), server.mobilePath(BuildConfig.V2_POPULAR_KEYWORDS_PATH)),
                        requests.map { it.path },
                    )
                    requests.forEach {
                        assertEquals("GET", it.method)
                        assertEquals("Bearer " + if (signup) "first-signup" else "old-access", it.authorization)
                        assertEquals("", it.body)
                    }
                    assertEquals(if (signup) SessionStatus.SIGNING_UP else SessionStatus.SIGNED_IN, session.state.value.status)
                }
            }
        }

    @Test
    fun existingClientReadsTheLatestSignupTokenOnEveryRequest() =
        runBlocking {
            LocalApiServer().use { server ->
                server.respond = { ApiReply(body = nicknameJson) }
                val session = SessionManager(MemorySessionStorage(pendingSession), refreshTokens = { error("unused") })
                val api = api(server, session, signup = true)
                api.recommendNickname()
                assertEquals("Bearer first-signup", server.takeRequest().authorization)
                session.acceptLogin(
                    AuthResponse(SignupStatus.PENDING, "Bearer", null, null, null, null, null, "latest-signup", 900L),
                    "KAKAO",
                )
                api.recommendNickname()
                assertEquals("Bearer latest-signup", server.takeRequest().authorization)
            }
        }

    @Test
    fun unavailableNicknameAndEmptyPopularListKeepThePendingSessionUsable() =
        runBlocking {
            LocalApiServer().use { server ->
                server.respond = { request ->
                    if (request.path == server.mobilePath(BuildConfig.V2_NICKNAME_RECOMMENDATION_PATH)) {
                        ApiReply(503, """{"success":false,"code":4208,"message":"추천 실패","data":null}""")
                    } else {
                        ApiReply(body = """{"success":true,"code":0,"message":"요청 성공!","data":[]}""")
                    }
                }
                val session = SessionManager(MemorySessionStorage(pendingSession), refreshTokens = { error("unused") })
                val api = api(server, session, signup = true)
                val unavailable = api.recommendNickname()
                assertEquals(503, unavailable.code())
                assertEquals(4208, Gson().fromJson(unavailable.errorBody()!!.string(), ApiError::class.java).code)
                assertTrue(
                    api
                        .popularKeywords()
                        .body()!!
                        .requireData()
                        .isEmpty(),
                )
                assertEquals(SessionStatus.SIGNING_UP, session.state.value.status)
                assertEquals("first-signup", session.signupToken())
            }
        }

    private fun api(
        server: LocalApiServer,
        session: SessionManager,
        signup: Boolean,
    ): SignupSuggestionApi =
        Retrofit
            .Builder()
            .baseUrl(RetrofitInstance.mobileBaseUrl(server.url + "api/v2/"))
            .client(OkHttpClient.Builder().addInterceptor(SessionInterceptor({ session }, signup)).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SignupSuggestionApi::class.java)

    private val pendingSession = StoredSession(signupToken = "first-signup", signupExpiresAt = Long.MAX_VALUE)
    private val nicknameJson = """{"success":true,"code":0,"message":"요청 성공!","data":{"nickname":"설레는수달"}}"""
    private val popularJson = """{"success":true,"code":0,"message":"요청 성공!","data":[{"alertKeyword":"소금빵"},{"alertKeyword":"산리오"}]}"""
}
