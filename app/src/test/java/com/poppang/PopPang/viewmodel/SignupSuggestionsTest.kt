package com.poppang.PopPang.viewmodel

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.KeywordSelectResponse
import com.poppang.PopPang.model.NicknameCheckResponse
import com.poppang.PopPang.model.NicknameRecommendationResponse
import com.poppang.PopPang.network.DuplicateNicknameApi
import com.poppang.PopPang.network.SignupSuggestionApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class SignupSuggestionsTest {
    private val access = FakeSuggestionApi()
    private val signup = FakeSuggestionApi()
    private val duplicate = FakeNicknameApi()
    private val signupDuplicate = FakeNicknameApi()

    private fun nickname() = DuplicateNickname(duplicate, signupDuplicate, access, signup, Dispatchers.Unconfined)

    private fun popular() = PopularKeywordViewModel(access, signup, Dispatchers.Unconfined)

    @Test fun initialSignupRecommendationPrefillsOnceAndStillNeedsDuplicateConfirmation() {
        val vm = nickname()
        vm.recommendInitialSignupNickname()
        vm.recommendInitialSignupNickname()
        assertEquals("설레는수달", vm.nickname)
        assertEquals(1, signup.nicknameCalls)
        assertEquals(0, access.nicknameCalls)
        assertFalse(vm.isSuccess)
        vm.checkNickname(forSignup = true)
        assertTrue(vm.isSuccess)
    }

    @Test fun initialSignupRecommendationPreservesAnEnteredAndConfirmedNickname() {
        val vm = nickname()
        vm.onNicknameChange("직접정한수달")
        vm.checkNickname(forSignup = true)
        vm.recommendInitialSignupNickname()
        assertEquals("직접정한수달", vm.nickname)
        assertTrue(vm.isSuccess)
        assertEquals(0, signup.nicknameCalls)
    }

    @Test fun initialSignupRecommendationDoesNotOverwriteInputTypedWhileLoading() {
        val gate = CompletableDeferred<Unit>()
        signup.nicknameGate = gate
        val vm = nickname()
        vm.recommendInitialSignupNickname()
        vm.recommendInitialSignupNickname()
        assertEquals(1, signup.nicknameCalls)
        vm.onNicknameChange("새로입력한수달")
        gate.complete(Unit)
        assertEquals("새로입력한수달", vm.nickname)
        assertFalse(vm.isSuccess)
        assertFalse(vm.isRecommending)
    }

    @Test fun initialSignupRecommendationFailureAllowsManualInputAndRetry() {
        signup.nicknameReply = Response.error(503, """{"code":4208}""".toResponseBody())
        val vm = nickname()
        vm.recommendInitialSignupNickname()
        assertEquals("", vm.nickname)
        assertNotNull(vm.recommendationError)
        assertFalse(vm.isRecommending)
        vm.onNicknameChange("직접입력한수달")
        vm.checkNickname(forSignup = true)
        assertTrue(vm.isSuccess)
        signup.nicknameReply = nicknameSuccess()
        vm.recommendNickname(forSignup = true)
        assertEquals("설레는수달", vm.nickname)
        assertFalse(vm.isSuccess)
        assertEquals(2, signup.nicknameCalls)
    }

    @Test fun recommendedSignupNicknameRequiresSeparateDuplicateConfirmation() {
        val vm = nickname()
        vm.recommendNickname(forSignup = true)
        assertEquals("설레는수달", vm.nickname)
        assertEquals(1, signup.nicknameCalls)
        assertEquals(0, access.nicknameCalls)
        assertFalse(vm.isSuccess)
        vm.checkNickname(forSignup = true)
        assertTrue(vm.isSuccess)
        assertEquals(listOf("설레는수달"), signupDuplicate.checked)
        assertTrue(duplicate.checked.isEmpty())
    }

    @Test fun unavailableRecommendationPreservesManualInputAndAllowsRetry() {
        val vm = nickname()
        vm.onNicknameChange("직접정한수달")
        signup.nicknameReply = Response.error(503, """{"code":4208}""".toResponseBody())
        vm.recommendNickname(forSignup = true)
        assertEquals("직접정한수달", vm.nickname)
        assertNotNull(vm.recommendationError)
        assertFalse(vm.isRecommending)
        signup.nicknameReply = nicknameSuccess()
        vm.recommendNickname(forSignup = true)
        assertEquals("설레는수달", vm.nickname)
        assertNull(vm.recommendationError)
        assertEquals(2, signup.nicknameCalls)
    }

    @Test fun lateRecommendationNeverOverwritesEditsEvenIfTextChangesBack() {
        val gate = CompletableDeferred<Unit>()
        signup.nicknameGate = gate
        val vm = nickname()
        vm.onNicknameChange("원래닉네임")
        vm.recommendNickname(forSignup = true)
        vm.recommendNickname(forSignup = true)
        assertTrue(vm.isRecommending)
        assertEquals(1, signup.nicknameCalls)
        vm.onNicknameChange("수정한닉네임")
        vm.onNicknameChange("원래닉네임")
        gate.complete(Unit)
        assertEquals("원래닉네임", vm.nickname)
        assertFalse(vm.isSuccess)
        assertEquals("", vm.checkResult)
        assertFalse(vm.isRecommending)
    }

    @Test fun lateDuplicateResponseDoesNotValidateAnEditedNickname() {
        val gate = CompletableDeferred<Unit>()
        signupDuplicate.gate = gate
        val vm = nickname()
        vm.onNicknameChange("첫닉네임")
        vm.checkNickname(forSignup = true)
        vm.onNicknameChange("다른닉네임")
        gate.complete(Unit)
        assertFalse(vm.isSuccess)
        assertEquals("", vm.checkResult)
        assertFalse(vm.isChecking)
    }

    @Test fun serverDuplicateCheckCanRejectAPreviouslyRecommendedNickname() {
        signupDuplicate.duplicated = true
        val vm = nickname()
        vm.recommendNickname(forSignup = true)
        vm.checkNickname(forSignup = true)
        assertTrue(vm.isError)
        assertFalse(vm.isSuccess)
    }

    @Test fun signedInNicknameRecommendationUsesAccessClient() {
        val vm = nickname()
        vm.recommendNickname()
        assertEquals(1, access.nicknameCalls)
        assertEquals(0, signup.nicknameCalls)
    }

    @Test fun popularKeywordsKeepServerRankingAndCacheSuccessfulEmptyLists() {
        val vm = popular()
        signup.popularReply = popularSuccess(listOf("산리오", "소금빵", "abc"))
        vm.load(forSignup = true)
        assertEquals(listOf("산리오", "소금빵", "abc"), vm.keywords)
        assertEquals(0, access.popularCalls)
        signup.popularReply = popularSuccess(emptyList())
        vm.load(forSignup = true, force = true)
        vm.load(forSignup = true)
        assertTrue(vm.keywords.isEmpty())
        assertNull(vm.errorMessage)
        assertEquals(2, signup.popularCalls)
    }

    @Test fun popularFailureIsRetryableWithoutFillingInSampleKeywords() {
        signup.popularFailure = IOException("offline")
        val vm = popular()
        vm.load(forSignup = true)
        assertFalse(vm.isLoading)
        assertTrue(vm.keywords.isEmpty())
        assertNotNull(vm.errorMessage)
        signup.popularFailure = null
        vm.load(forSignup = true, force = true)
        assertEquals(listOf("소금빵", "산리오"), vm.keywords)
        assertNull(vm.errorMessage)
    }

    @Test fun reenteringKeywordStepWhileLoadingDoesNotSendAnotherRequest() {
        val gate = CompletableDeferred<Unit>()
        signup.popularGate = gate
        val vm = popular()
        vm.load(forSignup = true)
        vm.load(forSignup = true)
        assertEquals(1, signup.popularCalls)
        assertTrue(vm.isLoading)
        gate.complete(Unit)
        assertFalse(vm.isLoading)
        vm.load(forSignup = true)
        assertEquals(1, signup.popularCalls)
    }

    @Test fun signedInPopularQueryUsesAccessClient() {
        val vm = popular()
        vm.load()
        assertEquals(1, access.popularCalls)
        assertEquals(0, signup.popularCalls)
    }
}

private fun nicknameSuccess() = Response.success(ApiResponse(true, 0, null, NicknameRecommendationResponse("설레는수달")))

private fun popularSuccess(values: List<String>) = Response.success(ApiResponse(true, 0, null, values.map { KeywordSelectResponse(it) }))

private class FakeSuggestionApi : SignupSuggestionApi {
    var nicknameCalls = 0
    var popularCalls = 0
    var nicknameGate: CompletableDeferred<Unit>? = null
    var popularGate: CompletableDeferred<Unit>? = null
    var nicknameReply = nicknameSuccess()
    var popularReply = popularSuccess(listOf("소금빵", "산리오"))
    var popularFailure: Exception? = null

    override suspend fun recommendNickname(): Response<ApiResponse<NicknameRecommendationResponse>> {
        nicknameCalls++
        nicknameGate?.await()
        return nicknameReply
    }

    override suspend fun popularKeywords(): Response<ApiResponse<List<KeywordSelectResponse>>> {
        popularCalls++
        popularGate?.await()
        popularFailure?.let { throw it }
        return popularReply
    }
}

private class FakeNicknameApi : DuplicateNicknameApi {
    val checked = mutableListOf<String>()
    var duplicated = false
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun checkNickname(nickname: String): Response<ApiResponse<NicknameCheckResponse>> {
        checked += nickname
        gate?.await()
        return Response.success(ApiResponse(true, 0, null, NicknameCheckResponse(duplicated)))
    }
}
