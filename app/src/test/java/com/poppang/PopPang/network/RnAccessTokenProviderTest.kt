package com.poppang.PopPang.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class RnAccessTokenProviderTest {
    @Test
    fun initialRequestUsesStoredAccessTokenWithoutRefreshing() {
        val manager = SessionManager(MemorySessionStorage(storedSession()), { error("Unexpected refresh") })

        assertEquals("old-access", provider(manager).getAccessToken(null, false))
    }

    @Test
    fun rejectedOlderTokenUsesAlreadyRotatedTokenEvenWhenNotExpired() {
        val manager = SessionManager(MemorySessionStorage(storedSession()), { rotatedTokens() })
        manager.refresh(requireNotNull(manager.requestSession()))

        assertEquals("new-access", provider(manager).getAccessToken("old-access", false))
        assertEquals(SessionStatus.SIGNED_IN, manager.state.value.status)
    }

    @Test
    fun expiredTokenRotatesAccessAndRefreshTokensTogether() {
        val storage = MemorySessionStorage(storedSession())
        val manager =
            SessionManager(storage, {
                assertEquals("old-refresh", it)
                rotatedTokens()
            })

        assertEquals("new-access", provider(manager).getAccessToken("old-access", true))
        assertEquals("new-refresh", storage.value?.refreshToken)
    }

    @Test
    fun invalidAccessTokenSignsOutInsteadOfRefreshing() {
        val manager = SessionManager(MemorySessionStorage(storedSession()), { error("Unexpected refresh") })

        assertEquals("login_required", failure { provider(manager).getAccessToken("old-access", false) }.code)
        assertNull(manager.requestSession())
    }

    @Test
    fun networkFailureAllowsRetryWithoutSigningOut() {
        val manager = SessionManager(MemorySessionStorage(storedSession()), { throw IOException("offline") })

        assertEquals("temporary_failure", failure { provider(manager).getAccessToken("old-access", true) }.code)
        assertEquals("old-access", manager.requestSession()?.accessToken)
    }

    @Test
    fun serverFailureAllowsRetryWithoutSigningOut() {
        val manager = SessionManager(MemorySessionStorage(storedSession()), { throw RefreshRejected(503) })

        assertEquals("temporary_failure", failure { provider(manager).getAccessToken("old-access", true) }.code)
        assertEquals(SessionStatus.SIGNED_IN, manager.state.value.status)
    }

    @Test
    fun rejectedRefreshTokenRequiresLogin() {
        val manager = SessionManager(MemorySessionStorage(storedSession()), { throw RefreshRejected(401) })

        assertEquals("login_required", failure { provider(manager).getAccessToken("old-access", true) }.code)
        assertNull(manager.requestSession())
    }

    @Test
    fun signedOutSessionCannotProvideToken() {
        val manager = SessionManager(MemorySessionStorage(), { error("Unexpected refresh") })

        assertEquals("login_required", failure { provider(manager).getAccessToken(null, false) }.code)
    }

    @Test
    fun nativeAndRnRequestsShareOneRefresh() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val refreshes = AtomicInteger()
        val manager =
            SessionManager(MemorySessionStorage(storedSession()), {
                refreshes.incrementAndGet()
                entered.countDown()
                check(release.await(5, TimeUnit.SECONDS))
                rotatedTokens()
            })
        val original = requireNotNull(manager.requestSession())
        val executor = Executors.newFixedThreadPool(2)
        try {
            val native = executor.submit<RequestSession?> { manager.refresh(original) }
            check(entered.await(5, TimeUnit.SECONDS))
            val rn = executor.submit<String> { provider(manager).getAccessToken("old-access", true) }
            release.countDown()

            assertEquals("new-access", native.get(5, TimeUnit.SECONDS)?.accessToken)
            assertEquals("new-access", rn.get(5, TimeUnit.SECONDS))
            assertEquals(1, refreshes.get())
        } finally {
            release.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun logoutDuringRefreshDoesNotReviveSession() {
        assertSessionChangeDuringRefresh { it.clear() }
    }

    @Test
    fun accountChangeDuringRefreshDoesNotReturnOtherUsersToken() {
        assertSessionChangeDuringRefresh { it.acceptLogin(loginData(), "GOOGLE") }
    }

    private fun assertSessionChangeDuringRefresh(change: (SessionManager) -> Unit) {
        lateinit var manager: SessionManager
        manager =
            SessionManager(MemorySessionStorage(storedSession()), {
                change(manager)
                rotatedTokens()
            })

        assertEquals("login_required", failure { provider(manager).getAccessToken("old-access", true) }.code)
        assertFalse(manager.requestSession()?.accessToken == "new-access")
    }

    private fun provider(manager: SessionManager) = RnAccessTokenProvider { manager }

    private fun failure(action: () -> Unit): RnAuthException = assertThrows(RnAuthException::class.java, action)
}
