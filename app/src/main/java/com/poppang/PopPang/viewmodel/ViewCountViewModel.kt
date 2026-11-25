package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.ViewCountIncrementApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewCountViewModel(
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Preserve the displayed total on a failed write.
            }
        }
    }
}
