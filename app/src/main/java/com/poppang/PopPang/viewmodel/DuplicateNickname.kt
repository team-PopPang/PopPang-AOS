package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.network.DuplicateNicknameApi
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.SignupSuggestionApi
import com.poppang.PopPang.network.apiErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DuplicateNickname(
    private val accessApi: DuplicateNicknameApi = RetrofitInstance.duplicateNicknameApi,
    private val signupApi: DuplicateNicknameApi = RetrofitInstance.signupNicknameApi,
    private val accessSuggestions: SignupSuggestionApi = RetrofitInstance.suggestionApi,
    private val signupSuggestions: SignupSuggestionApi = RetrofitInstance.signupSuggestionApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel() {
    var nickname by mutableStateOf("")
        private set
    var checkResult by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set
    var isSuccess by mutableStateOf(false)
        private set
    var isChecking by mutableStateOf(false)
        private set
    var isRecommending by mutableStateOf(false)
        private set
    var recommendationError by mutableStateOf<String?>(null)
        private set
    private var editRevision = 0L

    fun onNicknameChange(newNickname: String) {
        editRevision++
        nickname = newNickname
        checkResult = ""
        isError = false
        isSuccess = false
        recommendationError = null
    }

    fun recommendInitialSignupNickname() {
        if (nickname.isNotEmpty()) return
        recommendNickname(forSignup = true)
    }

    fun recommendNickname(forSignup: Boolean = false) {
        if (isRecommending || isChecking) return
        val revision = editRevision
        isRecommending = true
        recommendationError = null
        viewModelScope.launch(dispatcher) {
            try {
                val response = (if (forSignup) signupSuggestions else accessSuggestions).recommendNickname()
                if (editRevision != revision) return@launch
                if (!response.isSuccessful) {
                    recommendationError =
                        if (response.code() == 503) {
                            "닉네임 추천을 잠시 사용할 수 없어요. 직접 입력하거나 다시 시도해주세요."
                        } else {
                            apiErrorMessage(response, "닉네임을 추천하지 못했어요. 다시 시도해주세요.")
                        }
                    return@launch
                }
                val recommended = checkNotNull(response.body()).requireData().nickname
                check(!recommended.isNullOrBlank())
                onNicknameChange(recommended)
                // A recommendation is not a reservation. Keep the existing duplicate check required.
                checkResult = "추천 닉네임이에요. 중복확인을 눌러주세요."
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (editRevision == revision) {
                    recommendationError = "닉네임을 추천하지 못했어요. 직접 입력하거나 다시 시도해주세요."
                }
            } finally {
                isRecommending = false
            }
        }
    }

    fun checkNickname(forSignup: Boolean = false) {
        if (isChecking || isRecommending) return
        if (nickname.isBlank()) {
            checkResult = "닉네임을 입력해주세요."
            isError = true
            isSuccess = false
            return
        }
        val checkedNickname = nickname.trim()
        val revision = editRevision
        isChecking = true
        isSuccess = false
        isError = false
        checkResult = ""
        recommendationError = null
        viewModelScope.launch(dispatcher) {
            try {
                val response = (if (forSignup) signupApi else accessApi).checkNickname(checkedNickname)
                if (editRevision != revision) return@launch
                if (!response.isSuccessful) {
                    checkResult = apiErrorMessage(response, "닉네임 확인에 실패했습니다.")
                    isError = true
                    return@launch
                }
                val duplicated = checkNotNull(response.body()).requireData().isDuplicated
                checkResult = if (duplicated) "이미 사용 중인 닉네임입니다" else "사용 가능한 닉네임입니다"
                isError = duplicated
                isSuccess = !duplicated
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (editRevision != revision) return@launch
                checkResult = "닉네임 확인에 실패했습니다. 다시 시도해주세요."
                isError = true
            } finally {
                isChecking = false
            }
        }
    }
}
