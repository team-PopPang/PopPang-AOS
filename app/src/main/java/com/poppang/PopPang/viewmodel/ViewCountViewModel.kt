package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ViewCountViewModel: ViewModel() {
    fun incrementViewCount(popupUuid: String) {
        viewModelScope.launch {
            try {
                RetrofitInstance.viewCountIncrementApi.incrementViewCount(popupUuid)
                delay(300)
                Log.d("ViewCountViewModel", "View count incremented for popupUuid: $popupUuid")
            } catch (e: Exception) {

            }
        }
    }

    fun getTotalViewCount(userUuid: String, popupUuid: String, onResult: (Double) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.viewCountApi.getTotalViewCount(userUuid,popupUuid)
                val count = response.viewCount ?: 0.0
                onResult(count)
                delay(300)
                Log.d("ViewCountViewModel", "Total view count for popupUuid $popupUuid: $count")
            } catch (e: Exception) {
                onResult(0.0)
                Log.e("ViewCountViewModel", "Error fetching total view count", e)
            }
        }
    }
}