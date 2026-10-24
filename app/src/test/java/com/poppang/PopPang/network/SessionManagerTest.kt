package com.poppang.PopPang.network

import com.poppang.PopPang.model.SignupStatus
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.*
import org.junit.Test

class SessionManagerTest {
    @Test
    fun concurrentExpiredRequestsShareOneRotationAndPersistBothTokens() {
        val storage = MemorySessionStorage(storedSession())
        val calls = AtomicInteger()
        val session = SessionManager(storage, refreshTokens = {
            calls.incrementAndGet()
            assertEquals("old-refresh", it)
            rotatedTokens()
        })
        val expired = session.requestSession()!!
        val pool = Executors.newFixedThreadPool(8)
        try {
            val requests = (1..8).map { pool.submit<RequestSession?> { session.refresh(expired) } }
            requests.forEach { assertEquals("new-access", it.get(5, TimeUnit.SECONDS)?.accessToken) }
            assertEquals(1, calls.get())
            assertEquals("new-access", storage.value?.accessToken)
            assertEquals("new-refresh", storage.value?.refreshToken)
        } finally { pool.shutdownNow() }
    }

    @Test
    fun failedRotationIsSharedButANewRequestCanRetryLater() {
        val storage = MemorySessionStorage(storedSession())
        val calls = AtomicInteger()
        val session = SessionManager(storage, refreshTokens = {
            calls.incrementAndGet()
            throw RefreshRejected(503)
        })
        val expired = session.requestSession()!!
        repeat(8) { assertNull(session.refresh(expired)) }
        assertEquals(1, calls.get())
        assertEquals("old-refresh", storage.value?.refreshToken)
        assertNull(session.refresh(session.requestSession()!!))
        assertEquals(2, calls.get())
    }

    @Test
    fun refresh401ClearsSessionWhileTransientFailuresKeepIt() {
        for (status in listOf(400, 401, 403, 429, 500, 503)) {
            val storage = MemorySessionStorage(storedSession())
            val session = SessionManager(storage, refreshTokens = { throw RefreshRejected(status) })
            assertNull(session.refresh(session.requestSession()!!))
            assertEquals(status != 401, session.requestSession() != null)
            assertEquals(status != 401, storage.value != null)
        }
        val storage = MemorySessionStorage(storedSession())
        val session = SessionManager(storage, refreshTokens = { throw IOException("offline") })
        assertNull(session.refresh(session.requestSession()!!))
        assertNotNull(storage.value)
    }

    @Test
    fun logoutDuringRefreshPreventsLateTokenRestoration() {
        val storage = MemorySessionStorage(storedSession())
        val entered = CountDownLatch(1)
        val resume = CountDownLatch(1)
        val session = SessionManager(storage, refreshTokens = {
            entered.countDown()
            check(resume.await(5, TimeUnit.SECONDS))
            rotatedTokens()
        })
        val pool = Executors.newSingleThreadExecutor()
        try {
            val original = session.requestSession()!!
            val result = pool.submit<RequestSession?> { session.refresh(original) }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            session.clearIfGeneration(original.generation)
            resume.countDown()
            assertNull(result.get(5, TimeUnit.SECONDS))
            assertNull(session.requestSession())
            assertNull(storage.value)
        } finally { resume.countDown(); pool.shutdownNow() }
    }

    @Test
    fun previousAccountFailureCannotClearNewLogin() {
        val session = SessionManager(MemorySessionStorage(storedSession()), refreshTokens = { rotatedTokens() })
        val old = session.requestSession()!!
        session.acceptLogin(loginData(), "GOOGLE")
        session.invalidate(old)
        session.clearIfGeneration(old.generation)
        assertNull(session.refresh(old))
        assertEquals("member-2", session.userUuid())
        assertEquals("login-access", session.requestSession()?.accessToken)
    }

    @Test
    fun newAccountRefreshDoesNotJoinTheOldAccountFlight() {
        val storage = MemorySessionStorage(storedSession())
        val entered = CountDownLatch(1)
        val resume = CountDownLatch(1)
        val session = SessionManager(storage, refreshTokens = {
            if (it == "old-refresh") {
                entered.countDown()
                check(resume.await(5, TimeUnit.SECONDS))
                rotatedTokens()
            } else {
                rotatedTokens().copy(accessToken = "account-2-access", refreshToken = "account-2-refresh")
            }
        })
        val pool = Executors.newFixedThreadPool(2)
        try {
            val original = session.requestSession()!!
            val oldResult = pool.submit<RequestSession?> { session.refresh(original) }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            session.acceptLogin(loginData(), "GOOGLE")
            val newRequest = session.requestSession()!!
            val newResult = pool.submit<RequestSession?> { session.refresh(newRequest) }
            assertEquals("account-2-access", newResult.get(3, TimeUnit.SECONDS)?.accessToken)
            resume.countDown()
            assertNull(oldResult.get(3, TimeUnit.SECONDS))
            assertEquals("account-2-refresh", storage.value?.refreshToken)
            assertEquals("member-2", session.userUuid())
        } finally { resume.countDown(); pool.shutdownNow() }
    }

    @Test
    fun signupSessionContainsNoAccessTokenAndExpiresWithoutRefresh() {
        val storage = MemorySessionStorage()
        var now = 1000L
        val session = SessionManager(storage, refreshTokens = { error("Signup must not refresh") }, now = { now })
        val pending = loginData().copy(
            signupStatus = SignupStatus.PENDING, user = null,
            accessToken = null, refreshToken = null,
            signupToken = "signup-token", signupTokenExpiresIn = 1,
        )
        val profile = session.acceptLogin(pending, "KAKAO")
        assertEquals(SignupStatus.PENDING, profile.signupStatus)
        assertNull(profile.userUuid)
        assertNull(session.requestSession())
        assertEquals("signup-token", session.signupToken())
        now = 2000
        assertNull(session.signupToken())
        assertNull(storage.value)
        assertEquals(SessionStatus.SIGNED_OUT, session.state.value.status)
    }

    @Test
    fun signupCompletionReplacesSignupTokenWithTheServerUserAndTokenPair() {
        val storage = MemorySessionStorage(StoredSession(signupToken = "signup-token", signupExpiresAt = Long.MAX_VALUE))
        val session = SessionManager(storage, refreshTokens = { error("unused") })
        val user = session.acceptLogin(loginData(), "GOOGLE")
        assertEquals("member-2", user.userUuid)
        assertNull(storage.value?.signupToken)
        assertEquals("login-refresh", storage.value?.refreshToken)
        assertEquals(SessionStatus.SIGNED_IN, session.state.value.status)
    }

    @Test
    fun diagnosticStringsNeverExposeCredentials() {
        assertFalse(storedSession().toString().contains("old-access"))
        assertFalse(RequestSession(0, "secret").toString().contains("secret"))
        assertFalse(SignupRequestSession(0, "secret").toString().contains("secret"))
    }
}
