package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.RegionsResponse
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegionsViewModel : ViewModel() {
    private val _regions = MutableStateFlow<List<RegionsResponse>>(emptyList())
    val regions: StateFlow<List<RegionsResponse>> = _regions

    fun getRegions() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.regionsApi.getRegions()
                _regions.value = response
                Log.e("RegionsViewModel", "Regions fetched: $response")
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}
