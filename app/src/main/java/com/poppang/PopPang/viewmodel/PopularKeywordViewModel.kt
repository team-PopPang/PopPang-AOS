package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.SignupSuggestionApi
import com.poppang.PopPang.network.apiErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PopularKeywordViewModel(
    private val accessApi: SignupSuggestionApi = RetrofitInstance.suggestionApi,
    private val signupApi: SignupSuggestionApi = RetrofitInstance.signupSuggestionApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel() {
    var keywords by mutableStateOf<List<String>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    private var loadedForSignup: Boolean? = null

    fun load(
        forSignup: Boolean = false,
        force: Boolean = false,
    ) {
        if (isLoading || (!force && loadedForSignup == forSignup)) return
        isLoading = true
        errorMessage = null
        viewModelScope.launch(dispatcher) {
            try {
                val response = (if (forSignup) signupApi else accessApi).popularKeywords()
                if (!response.isSuccessful) {
                    errorMessage = apiErrorMessage(response, "인기 키워드를 불러오지 못했어요.")
                    return@launch
                }
                // Ranking and normalization belong to the server. An empty list is a valid result.
                keywords = checkNotNull(response.body()).requireData().map { it.alertKeyword }.take(6)
                loadedForSignup = forSignup
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                errorMessage = "인기 키워드를 불러오지 못했어요. 직접 입력하거나 다시 시도해주세요."
            } finally {
                isLoading = false
            }
        }
    }
}
