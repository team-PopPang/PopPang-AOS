package com.poppang.PopPang.viewmodel

import com.poppang.PopPang.model.AlertStatusResponse
import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.network.AlertApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SignupNotificationViewModelTest {
    private val profile = LoginResponse(userUuid = "test-member", nickname = "테스트", isAlerted = false)

    @Test fun successfulOptInExplicitlyEnablesAlertsAndUpdatesTheMainScreenProfile() {
        val api = FakeAlertApi()
        val vm = SignupNotificationViewModel(api, Dispatchers.Unconfined)
        var finished: LoginResponse? = null
        vm.enable(profile) { finished = it }
        assertEquals(listOf(true), api.requests)
        assertEquals(profile.copy(isAlerted = true), finished)
        assertEquals(false, profile.isAlerted)
        assertFalse(vm.isEnabling)
        assertNull(vm.errorMessage)
    }

    @Test fun repeatedTapsCannotSendConcurrentRequestsOrFinishBeforeTheResponse() {
        val api = FakeAlertApi().apply { gate = CompletableDeferred() }
        val vm = SignupNotificationViewModel(api, Dispatchers.Unconfined)
        var finishes = 0
        repeat(3) { vm.enable(profile) { finishes++ } }
        assertTrue(vm.isEnabling)
        assertEquals(listOf(true), api.requests)
        assertEquals(0, finishes)
        api.gate!!.complete(Unit)
        assertEquals(1, finishes)
        assertFalse(vm.isEnabling)
    }

    @Test fun networkFailureKeepsAlertsOffAndCanBeRetried() {
        val api = FakeAlertApi().apply { failure = IOException("offline") }
        val vm = SignupNotificationViewModel(api, Dispatchers.Unconfined)
        var finished: LoginResponse? = null
        vm.enable(profile) { finished = it }
        assertNull(finished)
        assertNotNull(vm.errorMessage)
        assertFalse(vm.isEnabling)
        api.failure = null
        vm.enable(profile) { finished = it }
        assertEquals(listOf(true, true), api.requests)
        assertEquals(true, finished?.isAlerted)
        assertNull(vm.errorMessage)
    }

    @Test fun aBusinessErrorOrAnOffResponseNeverReportsSuccessfulOptIn() {
        for (response in listOf(
            ApiResponse<AlertStatusResponse>(false, 4000, "rejected", null),
            ApiResponse(true, 0, null, AlertStatusResponse(false)),
            ApiResponse<AlertStatusResponse>(true, 0, null, null),
        )) {
            val api = FakeAlertApi().apply { reply = response }
            val vm = SignupNotificationViewModel(api, Dispatchers.Unconfined)
            var finishes = 0
            vm.enable(profile) { finishes++ }
            assertEquals(0, finishes)
            assertNotNull(vm.errorMessage)
            assertFalse(vm.isEnabling)
            vm.clearError()
            assertNull(vm.errorMessage)
        }
    }

    private class FakeAlertApi : AlertApi {
        val requests = mutableListOf<Boolean>()
        var gate: CompletableDeferred<Unit>? = null
        var failure: Exception? = null
        var reply = ApiResponse(true, 0, null, AlertStatusResponse(true))

        override suspend fun updateAlertStatus(body: AlertStatusResponse): ApiResponse<AlertStatusResponse> {
            requests += body.isAlerted
            gate?.await()
            failure?.let { throw it }
            return reply
        }
    }
}
