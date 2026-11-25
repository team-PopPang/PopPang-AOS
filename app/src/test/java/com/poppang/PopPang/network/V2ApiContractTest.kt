package com.poppang.PopPang.network

import com.google.gson.JsonParser
import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.*
import com.poppang.PopPang.viewmodel.MapArea
import com.poppang.PopPang.viewmodel.MapPopupfilterViewModel
import okhttp3.HttpUrl.Companion.toHttpUrl
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class V2ApiContractTest {
    @Test
    fun refreshRequestUsesConfiguredMobileEndpoint() {
        val request = RetrofitInstance.authApi.refresh(RefreshRequest("test-token")).request()
        val expectedUrl = BuildConfig.V2_BASE_URL.toHttpUrl()
            .newBuilder()
            .addEncodedPathSegments(BuildConfig.V2_MOBILE_PATH.trimEnd('/'))
            .addEncodedPathSegments(BuildConfig.V2_AUTH_REFRESH_PATH)
            .build()

        assertEquals(expectedUrl, request.url)
    }

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
            assertEquals(server.mobilePath(BuildConfig.V2_SIGNUP_OS_PATH), recorded.path)
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
            val request = UserSignUpRequest(
                nickname = "테스트",
                fcmToken = null,
                alertKeywordList = emptyList(),
                recommendList = listOf(3L),
            )
            assertEquals(SignupStatus.COMPLETED, api.signUpWithKakao(request).body()?.requireData()?.signupStatus)
            assertEquals(SignupStatus.COMPLETED, api.signUpWithGoogle(request).body()?.requireData()?.signupStatus)
            for (endpoint in listOf(BuildConfig.V2_KAKAO_SIGNUP_PATH, BuildConfig.V2_GOOGLE_SIGNUP_PATH)) {
                val recorded = server.takeRequest()
                assertEquals("POST", recorded.method)
                assertEquals(server.mobilePath(endpoint), recorded.path)
                assertEquals("Bearer signup", recorded.authorization)
                val json = JsonParser.parseString(recorded.body).asJsonObject
                assertEquals("ANDROID", json["signupOs"].asString)
                assertTrue(json["isAlerted"].asBoolean)
                for (removed in listOf("uid", "userUuid", "provider", "email", "role")) assertFalse(json.has(removed))
            }
        }
    }

    @Test
    fun publicLoginAndRefreshUseDistinctResponsesAndNoAuthorization() = runBlocking {
        LocalApiServer().use { server ->
            val refreshPath = server.mobilePath(BuildConfig.V2_AUTH_REFRESH_PATH)
            server.respond = { request -> ApiReply(body = if (request.path == refreshPath) tokenResponse else pendingLogin) }
            val api = retrofit(server).create(AuthApi::class.java)
            val pending = api.kakaoLogin(KakaoLoginRequest("fake-social-credential")).body()!!.requireData()
            assertEquals(SignupStatus.PENDING, pending.signupStatus)
            assertNull(pending.user)
            assertEquals("signup", pending.signupToken)
            val tokens = api.refresh(RefreshRequest("fake-refresh")).execute().body()!!.requireData()
            assertEquals("new-refresh", tokens.refreshToken)
            assertNull(server.takeRequest().authorization)
            val refresh = server.takeRequest()
            assertEquals(refreshPath, refresh.path)
            assertNull(refresh.authorization)
            assertEquals(setOf("refreshToken"), JsonParser.parseString(refresh.body).asJsonObject.keySet())
        }
    }

    @Test
    fun signupQueriesUseV2WithSignupTokenAndPreservePendingSession() = runBlocking {
        LocalApiServer().use { server ->
            server.respond = { request -> ApiReply(body = when {
                request.path == server.mobilePath(BuildConfig.V2_CATEGORIES_PATH) -> """[{"id":3,"recommendName":"문화"}]"""
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
            assertEquals(server.mobilePath(BuildConfig.V2_NICKNAME_CHECK_PATH) + "?nickname=test", nickname.path)
            assertEquals("Bearer signup", nickname.authorization)
            val category = server.takeRequest()
            assertEquals("GET", category.method)
            assertEquals(server.mobilePath(BuildConfig.V2_CATEGORIES_PATH), category.path)
            assertEquals("Bearer signup", category.authorization)
            assertEquals(SessionStatus.SIGNING_UP, session.state.value.status)
            assertEquals("signup", session.signupToken())
            assertNull(session.requestSession())
        }
    }

    @Test
    fun signedInQueriesUseV2WithAccessToken() = runBlocking {
        LocalApiServer().use { server ->
            server.respond = { request -> ApiReply(body = if (request.path == server.mobilePath(BuildConfig.V2_CATEGORIES_PATH)) {
                """[{"id":3,"recommendName":"문화"}]"""
            } else """{"success":true,"code":0,"message":"ok","data":{"isDuplicated":false}}""") }
            val session = SessionManager(MemorySessionStorage(storedSession()), refreshTokens = { error("unused") })
            val client = OkHttpClient.Builder().addInterceptor(SessionInterceptor({ session })).build()
            val accessV2 = retrofit(server, client)
            val result = accessV2.create(DuplicateNicknameApi::class.java).checkNickname("test")
            assertFalse(result.body()!!.requireData().isDuplicated)
            val profile = server.takeRequest()
            assertEquals(server.mobilePath(BuildConfig.V2_NICKNAME_CHECK_PATH) + "?nickname=test", profile.path)
            assertEquals("Bearer old-access", profile.authorization)
            assertEquals(3L, accessV2.create(CategoryItemApi::class.java).getCategoryItems().single().id)
            val category = server.takeRequest()
            assertEquals(server.mobilePath(BuildConfig.V2_CATEGORIES_PATH), category.path)
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
            val favoritePath = server.mobilePath(BuildConfig.V2_FAVORITE_PATH)
            val keywordPath = server.mobilePath(BuildConfig.V2_ALERT_KEYWORDS_PATH)
            assertEquals(listOf(favoritePath, favoritePath, keywordPath, keywordPath), requests.map { it.path })
            requests.forEach { assertFalse(JsonParser.parseString(it.body).asJsonObject.has("userUuid")) }
            requests.drop(2).forEach { assertEquals(setOf("keyword"), JsonParser.parseString(it.body).asJsonObject.keySet()) }
        }
    }

    @Test
    fun rawPopupListHandlesNullsAndExact64BitCountsWhileAlertListUsesEnvelope() = runBlocking {
        LocalApiServer().use { server ->
            server.respond = { request -> ApiReply(body = if (request.path == server.mobilePath(BuildConfig.V2_ALERT_POPUPS_PATH)) {
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
            assertEquals(server.mobilePath(BuildConfig.V2_POPUPS_PATH), server.takeRequest().path)
            assertEquals(server.mobilePath(BuildConfig.V2_ALERT_POPUPS_PATH), server.takeRequest().path)
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
            assertEquals(server.mobilePath(BuildConfig.V2_USER_PATH), withdrawal.path)
            assertEquals("", withdrawal.body)
            val logout = server.takeRequest()
            assertEquals("POST", logout.method)
            assertEquals(server.mobilePath(BuildConfig.V2_LOGOUT_PATH), logout.path)
            assertEquals("", logout.body)
        }
    }

    @Test
    fun mapSendsSelectedCategoryIdsAndNameOrAddressQueryInServerOrder() = runBlocking {
        LocalApiServer().use { server ->
            val map = MapPopupfilterViewModel(retrofit(server).create(MapPopupfilterApi::class.java))
            val area = MapArea("서울", "영등포구", 37.52, 126.92, "MOST_VIEWED")
            server.respond = { ApiReply(body = """[{"id":2,"recommendName":"뷰티","color":"#123456"}]""") }
            assertEquals("#123456", map.categories().single().color)
            assertEquals(server.mobilePath(BuildConfig.V2_MAP_CATEGORIES_PATH), server.takeRequest().path)

            server.respond = { ApiReply(body = "[$popupWithNulls,${popupWithNulls.replace("popup-1", "popup-0")}]") }
            val matches = checkNotNull(map.popups(area, listOf(2, 3), "  성수 & 팝업  "))
            assertEquals(listOf("popup-1", "popup-0"), matches.map { it.popupUuid })
            val request = server.takeRequest()
            val url = (server.url.trimEnd('/') + request.path).toHttpUrl()
            assertEquals(server.mobilePath(BuildConfig.V2_MAP_FILTER_PATH), url.encodedPath)
            assertEquals(listOf("2", "3"), url.queryParameterValues("recommendIds"))
            assertEquals("성수 & 팝업", url.queryParameter("q"))
            assertEquals("MOST_VIEWED", url.queryParameter("mapSortStandard"))
            assertEquals("영등포구", url.queryParameter("district"))

            server.respond = { ApiReply(body = "[]") }
            assertEquals(emptyList<PopupEvent>(), map.popups(area, query = "  "))
            val all = (server.url.trimEnd('/') + server.takeRequest().path).toHttpUrl()
            assertNull(all.queryParameter("q"))
            assertNull(all.queryParameter("recommendIds"))

            server.respond = { ApiReply(status = 503) }
            assertNull(map.popups(area, listOf(2)))
        }
    }

    private fun retrofit(server: LocalApiServer, client: OkHttpClient = OkHttpClient()) = Retrofit.Builder()
        .baseUrl(RetrofitInstance.mobileBaseUrl(server.url + "api/v2/")).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    private val pendingLogin = """{"success":true,"code":0,"data":{"signupStatus":"PENDING","tokenType":"Bearer","signupToken":"signup","signupTokenExpiresIn":900}}"""
    private val tokenResponse = """{"success":true,"code":0,"data":{"tokenType":"Bearer","accessToken":"new-access","refreshToken":"new-refresh","accessTokenExpiresIn":900,"refreshTokenExpiresIn":3600}}"""
    private val completedLogin = """{"success":true,"code":0,"data":{"signupStatus":"COMPLETED","tokenType":"Bearer","user":{"userUuid":"member-1","provider":"KAKAO","email":null,"nickname":"테스트","role":"MEMBER","isAlerted":false},"accessToken":"access","refreshToken":"refresh","accessTokenExpiresIn":900,"refreshTokenExpiresIn":3600}}"""
    private val popupWithNulls = """{"popupUuid":"popup-1","name":"팝업","startDate":"2026-10-22","endDate":"2026-10-29","openTime":null,"closeTime":null,"address":"주소","roadAddress":null,"region":"서울","latitude":null,"longitude":null,"instaPostId":null,"instaPostUrl":null,"captionSummary":"설명","imageUrlList":[],"mediaType":null,"recommendList":null,"favoriteCount":9007199254740993,"viewCount":12,"isFavorited":true}"""
}
