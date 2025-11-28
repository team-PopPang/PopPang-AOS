package com.poppang.PopPang.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapPopupfilterViewModel : ViewModel() {
    private val _popupList = MutableStateFlow<List<PopupEvent>>(emptyList())
    val mapPopupfilterList: StateFlow<List<PopupEvent>> = _popupList

    fun fetchmappopupfilter(userUuid: String, region: String, district: String, longitude: Double, latitude: Double, mapSortStandard: String) {
        Log.e("MapPopupfilterViewModel", "Fetching map popup filter for userUuid: $userUuid, region: $region, district: $district, longitude: $longitude, latitude: $latitude, mapSortStandard: $mapSortStandard")
        viewModelScope.launch{
            try{
                val response = RetrofitInstance.mapPopupFilterApi.getMapPopupfilter(
                    userUuid = userUuid,
                    region = region,
                    district = district,
                    longitude = longitude,
                    latitude = latitude,
                    mapSortStandard = mapSortStandard
                )
                Log.e("MapPopupfilterViewModel", "Fetched map popup filter: ${response.size}" )
                _popupList.value = response
                delay(300)
            }
            catch (e: Exception) {
                Log.e("MapPopupfilterViewModel", "Error fetching map popup filter: ${e.message}")
            }
        }
    }
}