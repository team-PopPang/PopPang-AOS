package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomePopupfilterViewModel : ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())
    val homePopupfilterList: StateFlow<List<PopupEvent>> = _popupList

    fun fetchhomepopupfilter(userUuid: String, region: String, district: String, homeSortStandard: String) {
        viewModelScope.launch{
            try{
                val response = RetrofitInstance.homePopupFilterApi.getHomePopupfilter(
                    userUuid,
                    region,
                    district,
                    homeSortStandard
                )
                Log.e("HomePopupfilterViewModel", "Fetched home popup filter: ${response.size}" )
                _popupList.value = response
            }
            catch (e: Exception) {
                Log.e("HomePopupfilterViewModel", "Error fetching home popup filter: ${e.message}")
            }
        }
    }
}