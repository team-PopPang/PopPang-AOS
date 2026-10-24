package com.poppang.PopPang.network

import java.util.concurrent.atomic.AtomicInteger
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test

class SessionInterceptorTest {
    @Test
    fun onlyExpiredAccess401RefreshesAndReplaysOnce() {
        LocalApiServer().use { server ->
            val calls = AtomicInteger()
            val session = SessionManager(MemorySessionStorage(storedSession()), refreshTokens = { calls.incrementAndGet(); rotatedTokens() })
            server.respond = { request ->
                if (request.authorization == "Bearer old-access") ApiReply(401, """{"code":5002}""")
                else ApiReply(200, """{"result":"ok"}""")
            }
            client(session).newCall(Request.Builder().url(server.url).build()).execute().use { assertEquals(200, it.code) }
            assertEquals("Bearer old-access", server.takeRequest().authorization)
            assertEquals("Bearer new-access", server.takeRequest().authorization)
            assertEquals(1, calls.get())
        }
    }

    @Test
    fun other401ClearsSessionAnd403DoesNotRefresh() {
        for ((http, code) in listOf(401 to 5001, 401 to 5006, 401 to 5010, 403 to 5008)) {
            LocalApiServer().use { server ->
                val session = SessionManager(MemorySessionStorage(storedSession()), refreshTokens = { error("Must not refresh") })
                server.respond = { ApiReply(http, """{"code":$code}""") }
                client(session).newCall(Request.Builder().url(server.url).build()).execute().use { assertEquals(http, it.code) }
                assertEquals(http == 403, session.requestSession() != null)
            }
        }
    }

    @Test
    fun second401DoesNotStartAnotherRefresh() {
        LocalApiServer().use { server ->
            val calls = AtomicInteger()
            val session = SessionManager(MemorySessionStorage(storedSession()), refreshTokens = { calls.incrementAndGet(); rotatedTokens() })
            server.respond = { ApiReply(401, """{"code":5002}""") }
            client(session).newCall(Request.Builder().url(server.url).build()).execute().use { assertEquals(401, it.code) }
            assertEquals(1, calls.get())
            assertNull(session.requestSession())
        }
    }

    @Test
    fun signup401NeverRefreshesAndDiscardsSignupToken() {
        LocalApiServer().use { server ->
            val storage = MemorySessionStorage(StoredSession(signupToken = "signup", signupExpiresAt = Long.MAX_VALUE))
            val session = SessionManager(storage, refreshTokens = { error("Must not refresh signup") })
            server.respond = { ApiReply(401, """{"code":5002}""") }
            client(session, signup = true).newCall(Request.Builder().url(server.url).build()).execute().use { assertEquals(401, it.code) }
            assertEquals("Bearer signup", server.takeRequest().authorization)
            assertNull(storage.value)
        }
    }

    @Test
    fun serverErrorsWithHtmlBodiesDoNotInvalidateSession() {
        LocalApiServer().use { server ->
            val session = SessionManager(MemorySessionStorage(storedSession()), refreshTokens = { error("unused") })
            server.respond = { ApiReply(503, "<html>unavailable</html>") }
            client(session).newCall(Request.Builder().url(server.url).build()).execute().use { assertEquals(503, it.code) }
            assertNotNull(session.requestSession())
        }
    }

    @Test
    fun responseFromPreviousAccountIsDiscardedAfterAccountChange() {
        LocalApiServer().use { server ->
            val session = SessionManager(MemorySessionStorage(storedSession()), refreshTokens = { error("unused") })
            server.respond = {
                session.acceptLogin(loginData(), "GOOGLE")
                ApiReply(200, """{"private":"previous-account"}""")
            }
            assertThrows(IOException::class.java) {
                client(session).newCall(Request.Builder().url(server.url).build()).execute().close()
            }
            assertEquals("member-2", session.userUuid())
        }
    }

    private fun client(session: SessionManager, signup: Boolean = false) = OkHttpClient.Builder()
        .retryOnConnectionFailure(false)
        .addInterceptor(SessionInterceptor({ session }, signup))
        .build()
}
