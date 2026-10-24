package com.poppang.PopPang.network

import com.poppang.PopPang.model.AuthResponse
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.SignupStatus
import com.poppang.PopPang.model.TokenResponse
import java.io.IOException
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SessionStatus { SIGNED_OUT, SIGNING_UP, SIGNED_IN }

data class SessionState(val generation: Long, val status: SessionStatus)

data class StoredSession(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val signupToken: String? = null,
    val signupExpiresAt: Long? = null,
    val provider: String? = null,
    val userUuid: String? = null,
) {
    override fun toString() = "StoredSession(redacted)"
}

interface SessionStorage {
    fun read(): StoredSession?
    fun write(session: StoredSession?)
}

data class RequestSession(val generation: Long, val accessToken: String, val refreshVersion: Long = 0) {
    override fun toString() = "RequestSession(generation=$generation)"
}

data class SignupRequestSession(val generation: Long, val token: String) {
    override fun toString() = "SignupRequestSession(generation=$generation)"
}

class RefreshRejected(val httpStatus: Int) : IOException("인증 정보를 갱신하지 못했습니다.")

/** Owns token rotation. A refresh may never revive a logged-out or replaced session. */
class SessionManager(
    private val storage: SessionStorage,
    private val refreshTokens: (String) -> TokenResponse,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private data class RefreshFlight(
        val generation: Long,
        val accessToken: String,
        val future: CompletableFuture<RequestSession?> = CompletableFuture(),
    )
    private val stateLock = Any()
    private val refreshLock = Any()
    private var record = storage.read()
    private var generation = 0L
    private var refreshVersion = 0L
    private var refreshFlight: RefreshFlight? = null
    private val mutableState = MutableStateFlow(SessionState(generation, status(record)))
    val state = mutableState.asStateFlow()

    fun requestSession(): RequestSession? = synchronized(stateLock) {
        record?.takeIf { !it.refreshToken.isNullOrBlank() }?.accessToken?.takeIf(String::isNotBlank)
            ?.let { RequestSession(generation, it, refreshVersion) }
    }

    fun userUuid(): String? = synchronized(stateLock) { record?.userUuid }

    fun signupProvider(): String? = synchronized(stateLock) { record?.provider }

    fun signupToken(): String? = signupRequest()?.token

    fun signupRequest(): SignupRequestSession? = synchronized(stateLock) {
        val session = record ?: return@synchronized null
        if (session.signupToken == null) return@synchronized null
        if ((session.signupExpiresAt ?: 0) <= now()) {
            clearLocked()
            return@synchronized null
        }
        SignupRequestSession(generation, session.signupToken)
    }

    fun acceptLogin(response: AuthResponse, provider: String): LoginResponse = synchronized(stateLock) {
        require(response.tokenType == "Bearer") { "지원하지 않는 인증 응답입니다." }
        val profile: LoginResponse
        val next = when (response.signupStatus) {
            SignupStatus.PENDING -> {
                val token = requireNotNull(response.signupToken?.takeIf(String::isNotBlank))
                val ttl = requireNotNull(response.signupTokenExpiresIn).also { require(it > 0) }
                profile = LoginResponse(provider = provider, signupStatus = SignupStatus.PENDING)
                StoredSession(
                    signupToken = token,
                    signupExpiresAt = now() + ttl * 1000,
                    provider = provider,
                )
            }
            SignupStatus.COMPLETED -> {
                profile = requireNotNull(response.user)
                require(!profile.userUuid.isNullOrBlank()) { "사용자 정보가 없습니다." }
                require(!response.accessToken.isNullOrBlank() && !response.refreshToken.isNullOrBlank())
                StoredSession(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    provider = profile.provider ?: provider,
                    userUuid = profile.userUuid,
                )
            }
        }
        storage.write(next)
        record = next
        generation += 1
        refreshVersion = 0
        publishState()
        profile.copy(signupStatus = response.signupStatus)
    }

    fun clear() = synchronized(stateLock) { clearLocked() }

    fun clearIfGeneration(expected: Long) = synchronized(stateLock) {
        if (generation == expected) clearLocked()
    }

    fun cancelSignup() = synchronized(stateLock) {
        if (record?.signupToken != null) clearLocked()
    }

    fun isCurrent(request: RequestSession): Boolean = synchronized(stateLock) {
        generation == request.generation && record?.accessToken != null
    }

    fun invalidate(request: RequestSession) = synchronized(stateLock) {
        if (generation == request.generation && record?.accessToken == request.accessToken) clearLocked()
    }

    /** Concurrent failures are shared too; a later, new request can explicitly retry. */
    fun refresh(request: RequestSession): RequestSession? {
        var owner = false
        var refreshToken: String? = null
        val flight = synchronized(refreshLock) {
            synchronized(stateLock) {
                if (generation != request.generation) return null
                val current = record ?: return null
                if (current.accessToken != request.accessToken) return requestSession()
                if (request.refreshVersion < refreshVersion) return null
                refreshToken = current.refreshToken
            }
            refreshFlight?.takeIf {
                it.generation == request.generation && it.accessToken == request.accessToken
            } ?: RefreshFlight(request.generation, request.accessToken).also {
                refreshFlight = it
                owner = true
            }
        }
        if (!owner) return flight.future.get()
        val result = try {
            refreshToken?.let { performRefresh(request, it) }
        } catch (_: Exception) {
            invalidate(request)
            null
        }
        synchronized(stateLock) {
            if (generation == request.generation) refreshVersion += 1
        }
        synchronized(refreshLock) {
            if (refreshFlight === flight) refreshFlight = null
            flight.future.complete(result)
        }
        return result
    }

    private fun performRefresh(request: RequestSession, refreshToken: String): RequestSession? {
        val tokens = try {
            refreshTokens(refreshToken).also {
                require(it.tokenType == "Bearer" && it.accessToken.isNotBlank() && it.refreshToken.isNotBlank())
            }
        } catch (error: RefreshRejected) {
            if (error.httpStatus == 401) invalidate(request)
            return null
        } catch (_: IOException) {
            return null
        } catch (_: IllegalArgumentException) {
            invalidate(request)
            return null
        }

        return synchronized(stateLock) {
            if (generation != request.generation || record?.accessToken != request.accessToken) return null
            val next = record!!.copy(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken)
            try {
                storage.write(next)
            } catch (_: IOException) {
                // The old refresh token has already been consumed by the server.
                clearLocked()
                return null
            }
            record = next
            requestSession()
        }
    }

    private fun clearLocked() {
        record = null
        generation += 1
        refreshVersion = 0
        try {
            storage.write(null)
        } finally {
            publishState()
        }
    }

    private fun publishState() {
        mutableState.value = SessionState(generation, status(record))
    }

    private fun status(value: StoredSession?) = when {
        !value?.accessToken.isNullOrBlank() && !value.refreshToken.isNullOrBlank() -> SessionStatus.SIGNED_IN
        !value?.signupToken.isNullOrBlank() -> SessionStatus.SIGNING_UP
        else -> SessionStatus.SIGNED_OUT
    }
}
