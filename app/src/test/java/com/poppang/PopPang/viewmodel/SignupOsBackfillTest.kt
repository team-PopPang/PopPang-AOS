package com.poppang.PopPang.viewmodel

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.SignupOsRequest
import com.poppang.PopPang.network.UserDataApi
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class SignupOsBackfillTest {
    @Test
    fun enteringMainRepeatedlySendsOnlyOneAndroidRequestWithoutCheckingStoredOs() {
        val api = FakeSignupOsApi()
        val viewModel = UserDataViewModel(api, Dispatchers.Unconfined)

        repeat(5) { viewModel.updateSignupOsOnce("member") }

        assertEquals(listOf(SignupOsRequest("ANDROID")), api.requests)
        assertEquals(0, api.profileReads)
    }

    @Test
    fun missingUserDoesNotConsumeTheFirstAuthenticatedMainEntry() {
        val api = FakeSignupOsApi()
        val viewModel = UserDataViewModel(api, Dispatchers.Unconfined)
        viewModel.updateSignupOsOnce("")
        viewModel.updateSignupOsOnce("   ")
        assertTrue(api.requests.isEmpty())

        viewModel.updateSignupOsOnce("member")

        assertEquals(1, api.requests.size)
    }

    @Test
    fun slowRequestReturnsImmediatelyAndReentryDoesNotStartAnotherOne() {
        val api = FakeSignupOsApi()
        val gate = CompletableDeferred<Unit>()
        api.gate = gate
        val viewModel = UserDataViewModel(api, Dispatchers.Unconfined)

        viewModel.updateSignupOsOnce("member")
        viewModel.updateSignupOsOnce("member")

        assertEquals(1, api.requests.size)
        assertFalse(api.completed)
        gate.complete(Unit)
        assertTrue(api.completed)
    }

    @Test
    fun networkFailureIsIgnoredUntilANewAppLaunchRetries() {
        val api = FakeSignupOsApi()
        api.failure = IOException("Offline")
        val firstLaunch = UserDataViewModel(api, Dispatchers.Unconfined)

        firstLaunch.updateSignupOsOnce("member")
        firstLaunch.updateSignupOsOnce("member")
        assertEquals(1, api.requests.size)

        api.failure = null
        val nextLaunch = UserDataViewModel(api, Dispatchers.Unconfined)
        nextLaunch.updateSignupOsOnce("member")
        nextLaunch.updateSignupOsOnce("member")
        assertEquals(2, api.requests.size)
        assertTrue(api.completed)
    }

    @Test
    fun httpFailureDoesNotRetryOnMainReentryOrProfileReset() {
        val api = FakeSignupOsApi()
        api.failure = HttpException(Response.error<Unit>(503, "{}".toResponseBody()))
        val viewModel = UserDataViewModel(api, Dispatchers.Unconfined)

        viewModel.updateSignupOsOnce("member")
        viewModel.reset()
        viewModel.updateSignupOsOnce("member")
        viewModel.updateSignupOsOnce("another-member")

        assertEquals(1, api.requests.size)
    }

    @Test
    fun successIsAlsoAttemptedAgainOnTheNextLaunchSoTheServerDecides() {
        val api = FakeSignupOsApi()
        UserDataViewModel(api, Dispatchers.Unconfined).updateSignupOsOnce("member")
        UserDataViewModel(api, Dispatchers.Unconfined).updateSignupOsOnce("member")

        assertEquals(2, api.requests.size)
        assertEquals(0, api.profileReads)
    }
}

private class FakeSignupOsApi : UserDataApi {
    val requests = mutableListOf<SignupOsRequest>()
    var profileReads = 0
    var failure: Exception? = null
    var gate: CompletableDeferred<Unit>? = null
    var completed = false

    override suspend fun updateSignupOs(request: SignupOsRequest) {
        requests += request
        gate?.await()
        failure?.let { throw it }
        completed = true
    }

    override suspend fun getUserData(): Response<ApiResponse<LoginResponse>> {
        profileReads++
        return Response.success(ApiResponse(true, 0, null, LoginResponse(userUuid = "member")))
    }
}
