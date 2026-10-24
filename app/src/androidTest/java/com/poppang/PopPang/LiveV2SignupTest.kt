package com.poppang.PopPang

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.poppang.PopPang.datastore.AuthTokenStore
import com.poppang.PopPang.model.ApiError
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.SessionStatus
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Opt-in, read-only checks against the configured real server; never creates an account. */
@RunWith(AndroidJUnit4::class)
class LiveV2SignupTest {
    @Before
    fun requireExplicitLiveRun() {
        assumeTrue(
            "Requires -e liveV2 true and a manually authenticated test account for session checks",
            InstrumentationRegistry.getArguments().getString("liveV2") == "true",
        )
        AuthTokenStore.initialize(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun signupQueriesRequireAuthentication() {
        val client = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build()
        for (path in listOf("user/nickname/duplicated?nickname=poppang_v2_probe", "recommend")) {
            val request = Request.Builder().url(BuildConfig.V2_BASE_URL.trimEnd('/') + "/" + path).build()
            client.newCall(request).execute().use { response ->
                assertEquals("GET $path", 401, response.code)
                val error = Gson().fromJson(response.body!!.string(), ApiError::class.java)
                assertEquals(5006, error.code)
                report("GET $path: HTTP ${response.code}, code=${error.code}, no Authorization")
            }
        }
    }

    @Test
    fun pendingSignupTokenCanQueryNicknameAndCategories() = runBlocking {
        val session = AuthTokenStore.session
        assertEquals("Complete social login with a new test account first", SessionStatus.SIGNING_UP, session.state.value.status)
        assertNotNull("Signup Token expired; sign in again", session.signupRequest())
        val nickname = "V2test${System.currentTimeMillis().toString().takeLast(6)}"
        val response = RetrofitInstance.signupNicknameApi.checkNickname(nickname)
        assertTrue("Nickname check HTTP ${response.code()}", response.isSuccessful)
        val result = response.body()!!.requireData()
        report("GET user/nickname/duplicated: HTTP ${response.code()}, code=0, parsed isDuplicated=${result.isDuplicated}")

        val categories = RetrofitInstance.signupCategoryApi.getCategoryItems()
        assertFalse("Server returned no signup categories", categories.isEmpty())
        assertTrue("Invalid category response", categories.all { it.id > 0 && it.recommendName.isNotBlank() })
        assertEquals(SessionStatus.SIGNING_UP, session.state.value.status)
        report("GET recommend: HTTP 200, parsed categoryCount=${categories.size}, session=SIGNING_UP")
    }

    @Test
    fun completedSignupSessionCanReadProfileAndCategories() = runBlocking {
        val session = AuthTokenStore.session
        assertEquals("Finish signup in the app with a test account first", SessionStatus.SIGNED_IN, session.state.value.status)
        val response = RetrofitInstance.userDataApi.getUserData()
        assertTrue("Profile HTTP ${response.code()}", response.isSuccessful)
        val profile = response.body()!!.requireData()
        assertEquals(session.userUuid(), profile.userUuid)
        assertFalse("Profile has no nickname", profile.nickname.isNullOrBlank())
        assertNotNull(session.requestSession())
        report("GET user: HTTP ${response.code()}, code=0, profile matches stored session")

        val categories = RetrofitInstance.categoryItemApi.getCategoryItems()
        assertFalse("Server returned no member categories", categories.isEmpty())
        assertEquals(SessionStatus.SIGNED_IN, session.state.value.status)
        report("GET recommend: HTTP 200, parsed categoryCount=${categories.size}, session=SIGNED_IN")
    }

    private fun report(message: String) {
        InstrumentationRegistry.getInstrumentation().sendStatus(0, Bundle().apply {
            putString("stream", "[Live V2] $message\n")
        })
    }
}
