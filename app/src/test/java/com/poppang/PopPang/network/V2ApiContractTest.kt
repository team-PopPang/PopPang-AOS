package com.poppang.PopPang.network

import com.google.gson.JsonParser
import com.poppang.PopPang.model.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class V2ApiContractTest {
    @Test
    fun signupOsBackfillUsesAccessTokenPatchAndAcceptsEmpty200Body() = runBlocking {
        LocalApiServer().use { server ->
            server.respond = { ApiReply(body = "") }
            val session = SessionManager(MemorySessionStorage(storedSession()), refreshTokens = { error("unused") })
            val client = OkHttpClient.Builder().addInterceptor(SessionInterceptor({ session })).build()
            val api = retrofit(server, client).create(UserDataApi::class.java)

            api.updateSignupOs(SignupOsRequest())

            val recorded = server.takeRequest()
            assertEquals("PATCH", recorded.method)
            assertEquals("/api/v2/user/signup-os", recorded.path)
            assertEquals("Bearer old-access", recorded.authorization)
            assertEquals("application/json", recorded.contentType?.substringBefore(';'))
            val json = JsonParser.parseString(recorded.body).asJsonObject
            assertEquals(setOf("signupOs"), json.keySet())
            assertEquals("ANDROID", json["signupOs"].asString)
        }
    }

    @Test
    fun signupBothProvidersSendAndroidOsAndSignupTokenWithoutLegacyIdentityFields() = runBlocking {
        LocalApiServer().use { server ->
            val session = SessionManager(
                MemorySessionStorage(StoredSession(signupToken = "signup", signupExpiresAt = Long.MAX_VALUE)),
                refreshTokens = { error("unused") },
            )
            server.respond = { ApiReply(body = completedLogin) }
            val client = OkHttpClient.Builder().addInterceptor(SessionInterceptor({ session }, signup = true)).build()
            val api = retrofit(server, client).create(UserSignUpApi::class.java)
            val request = UserSignUpRequest("테스트", false, null, emptyList(), listOf(3L))
            assertEquals(SignupStatus.COMPLETED, api.signUpWithKakao(request).body()?.requireData()?.signupStatus)
            assertEquals(SignupStatus.COMPLETED, api.signUpWithGoogle(request).body()?.requireData()?.signupStatus)
            for (provider in listOf("kakao", "google")) {
                val recorded = server.takeRequest()
                assertEquals("POST", recorded.method)
                assertEquals("/api/v2/auth/$provider/signup", recorded.path)
                assertEquals("Bearer signup", recorded.authorization)
                val json = JsonParser.parseString(recorded.body).asJsonObject
                assertEquals("ANDROID", json["signupOs"].asString)
                for (removed in listOf("uid", "userUuid", "provider", "email", "role")) assertFalse(json.has(removed))
            }
        }
    }

    @Test
    fun publicLoginAndRefreshUseDistinctResponsesAndNoAuthorization() = runBlocking {
        LocalApiServer().use { server ->
            server.respond = { request -> ApiReply(body = if (request.path.endsWith("refresh")) tokenResponse else pendingLogin) }
            val api = retrofit(server).create(AuthApi::class.java)
            val pending = api.kakaoLogin(KakaoLoginRequest("fake-social-credential")).body()!!.requireData()
            assertEquals(SignupStatus.PENDING, pending.signupStatus)
            assertNull(pending.user)
            assertEquals("signup", pending.signupToken)
            val tokens = api.refresh(RefreshRequest("fake-refresh")).execute().body()!!.requireData()
            assertEquals("new-refresh", tokens.refreshToken)
            assertNull(server.takeRequest().authorization)
            val refresh = server.takeRequest()
            assertEquals("/api/v2/auth/refresh", refresh.path)
            assertNull(refresh.authorization)
            assertEquals(setOf("refreshToken"), JsonParser.parseString(refresh.body).asJsonObject.keySet())
        }
    }

    @Test
    fun signupQueriesUseV2WithSignupTokenAndPreservePendingSession() = runBlocking {
        LocalApiServer().use { server ->
            server.respond = { request -> ApiReply(body = when {
                request.path == "/api/v2/recommend" -> """[{"id":3,"recommendName":"문화"}]"""
                else -> """{"success":true,"code":0,"message":"ok","data":{"isDuplicated":false}}"""
            }) }
            val session = SessionManager(
                MemorySessionStorage(StoredSession(signupToken = "signup", signupExpiresAt = Long.MAX_VALUE)),
                refreshTokens = { error("Must not refresh signup queries") },
            )
            val client = OkHttpClient.Builder().addInterceptor(SessionInterceptor({ session }, signup = true)).build()
            val signupV2 = retrofit(server, client)
            assertFalse(signupV2.create(DuplicateNicknameApi::class.java).checkNickname("test").body()!!.requireData().isDuplicated)
            assertEquals(3L, signupV2.create(CategoryItemApi::class.java).getCategoryItems().single().id)
            val nickname = server.takeRequest()
            assertEquals("GET", nickname.method)
            assertEquals("/api/v2/user/nickname/duplicated?nickname=test", nickname.path)
            assertEquals("Bearer signup", nickname.authorization)
            val category = server.takeRequest()
            assertEquals("GET", category.method)
            assertEquals("/api/v2/recommend", category.path)
            assertEquals("Bearer signup", category.authorization)
            assertEquals(SessionStatus.SIGNING_UP, session.state.value.status)
            assertEquals("signup", session.signupToken())
            assertNull(session.requestSession())
        }
    }

    @Test
    fun signedInQueriesUseV2WithAccessToken() = runBlocking {
        LocalApiServer().use { server ->
            server.respond = { request -> ApiReply(body = if (request.path == "/api/v2/recommend") {
                """[{"id":3,"recommendName":"문화"}]"""
            } else """{"success":true,"code":0,"message":"ok","data":{"isDuplicated":false}}""") }
            val session = SessionManager(MemorySessionStorage(storedSession()), refreshTokens = { error("unused") })
            val client = OkHttpClient.Builder().addInterceptor(SessionInterceptor({ session })).build()
            val accessV2 = retrofit(server, client)
            val result = accessV2.create(DuplicateNicknameApi::class.java).checkNickname("test")
            assertFalse(result.body()!!.requireData().isDuplicated)
            val profile = server.takeRequest()
            assertEquals("/api/v2/user/nickname/duplicated?nickname=test", profile.path)
            assertEquals("Bearer old-access", profile.authorization)
            assertEquals(3L, accessV2.create(CategoryItemApi::class.java).getCategoryItems().single().id)
            val category = server.takeRequest()
            assertEquals("/api/v2/recommend", category.path)
            assertEquals("Bearer old-access", category.authorization)
        }
    }

    @Test
    fun favoriteAndKeywordWritesRemoveUserUuidAndUseV2FieldNames() = runBlocking {
        LocalApiServer().use { server ->
            server.respond = { ApiReply(body = "") }
            val retrofit = retrofit(server)
            val favorite = retrofit.create(FavoriteApi::class.java)
            favorite.addFavorite(FavoriteRequest("popup-1"))
            favorite.deleteFavorite(FavoriteRequest("popup-1"))
            val keyword = retrofit.create(KeywordApi::class.java)
            keyword.insertKeyword(KeywordInsertRequest("브랜드"))
            keyword.deleteKeyword(KeywordDeleteRequest("브랜드"))
            val requests = (1..4).map { server.takeRequest() }
            assertEquals(listOf("POST", "DELETE", "POST", "DELETE"), requests.map { it.method })
            assertEquals(listOf("/api/v2/favorite", "/api/v2/favorite", "/api/v2/alert-keyword", "/api/v2/alert-keyword"), requests.map { it.path })
            requests.forEach { assertFalse(JsonParser.parseString(it.body).asJsonObject.has("userUuid")) }
            requests.drop(2).forEach { assertEquals(setOf("keyword"), JsonParser.parseString(it.body).asJsonObject.keySet()) }
        }
    }

    @Test
    fun rawPopupListHandlesNullsAndExact64BitCountsWhileAlertListUsesEnvelope() = runBlocking {
        LocalApiServer().use { server ->
            server.respond = { request -> ApiReply(body = if (request.path.contains("alert/popups")) {
                """{"success":true,"code":0,"data":[$popupWithNulls]}"""
            } else "[$popupWithNulls]") }
            val retrofit = retrofit(server)
            val popup = retrofit.create(PopupApi::class.java).getPopupEvent().single()
            assertEquals(9_007_199_254_740_993L, popup.favoriteCount)
            assertNull(popup.latitude)
            assertNull(popup.longitude)
            assertEquals(emptyList<String>(), popup.recommendList)
            assertEquals("주소", popup.roadAddress)
            assertEquals("", popup.openTime)
            assertFalse(popup.isRead)
            val alert = retrofit.create(AlertPopupApi::class.java).getAlertPopup().requireData().single()
            assertEquals(popup.popupUuid, alert.popupUuid)
            assertEquals("/api/v2/user/popups", server.takeRequest().path)
            assertEquals("/api/v2/user/alert/popups", server.takeRequest().path)
        }
    }

    @Test
    fun withdrawalAndLogoutAreEmptyBodyV2Requests() = runBlocking {
        LocalApiServer().use { server ->
            server.respond = { ApiReply(body = "") }
            val retrofit = retrofit(server)
            assertTrue(retrofit.create(UserDrawApi::class.java).withdrawUser().isSuccessful)
            assertTrue(retrofit.create(LogoutApi::class.java).logout().isSuccessful)
            val withdrawal = server.takeRequest()
            assertEquals("DELETE", withdrawal.method)
            assertEquals("/api/v2/user", withdrawal.path)
            assertEquals("", withdrawal.body)
            val logout = server.takeRequest()
            assertEquals("POST", logout.method)
            assertEquals("/api/v2/auth/logout", logout.path)
            assertEquals("", logout.body)
        }
    }

    private fun retrofit(server: LocalApiServer, client: OkHttpClient = OkHttpClient()) = Retrofit.Builder()
        .baseUrl(server.url + "api/v2/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    private val pendingLogin = """{"success":true,"code":0,"data":{"signupStatus":"PENDING","tokenType":"Bearer","signupToken":"signup","signupTokenExpiresIn":900}}"""
    private val tokenResponse = """{"success":true,"code":0,"data":{"tokenType":"Bearer","accessToken":"new-access","refreshToken":"new-refresh","accessTokenExpiresIn":900,"refreshTokenExpiresIn":3600}}"""
    private val completedLogin = """{"success":true,"code":0,"data":{"signupStatus":"COMPLETED","tokenType":"Bearer","user":{"userUuid":"member-1","provider":"KAKAO","email":null,"nickname":"테스트","role":"MEMBER","isAlerted":false},"accessToken":"access","refreshToken":"refresh","accessTokenExpiresIn":900,"refreshTokenExpiresIn":3600}}"""
    private val popupWithNulls = """{"popupUuid":"popup-1","name":"팝업","startDate":"2026-10-22","endDate":"2026-10-29","openTime":null,"closeTime":null,"address":"주소","roadAddress":null,"region":"서울","latitude":null,"longitude":null,"instaPostId":null,"instaPostUrl":null,"captionSummary":"설명","imageUrlList":[],"mediaType":null,"recommendList":null,"favoriteCount":9007199254740993,"viewCount":12,"isFavorited":true}"""
}
