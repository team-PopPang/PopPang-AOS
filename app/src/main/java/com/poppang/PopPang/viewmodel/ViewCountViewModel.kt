package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.ViewCountApi
import com.poppang.PopPang.network.ViewCountIncrementApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewCountViewModel(
    private val api: ViewCountApi = RetrofitInstance.viewCountApi,
    private val incrementApi: ViewCountIncrementApi = RetrofitInstance.viewCountIncrementApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel() {
    private val _viewCounts = MutableStateFlow<Map<String, Long>>(emptyMap())
    val viewCounts = _viewCounts.asStateFlow()

    fun updateCountsFromList(popups: List<PopupEvent>) {
        _viewCounts.value = _viewCounts.value + popups.associate { it.popupUuid to it.viewCount }
    }

    fun incrementViewCount(popupUuid: String) {
        viewModelScope.launch(dispatcher) {
            try {
                incrementApi.incrementViewCount(popupUuid)
                // The server flushes recorded views later; do not invent an immediate +1 total.
                getTotalViewCount("", popupUuid) {}
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Preserve the displayed total on a failed write.
            }
        }
    }

    fun getTotalViewCount(userUuid: String, popupUuid: String, onResult: (Long) -> Unit) {
        viewModelScope.launch(dispatcher) {
            try {
                val response = api.getTotalViewCount(popupUuid)
                val count = response.totalViewCount
                _viewCounts.value = _viewCounts.value + (popupUuid to count)
                onResult(count)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _viewCounts.value[popupUuid]?.let(onResult)
            }
        }
    }
}
