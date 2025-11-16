package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.FavoriteRequest
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel: ViewModel() {
    private val _favoritePopupUuids = MutableStateFlow<List<String>>(emptyList())
    val favoritePopupUuids: StateFlow<List<String>> = _favoritePopupUuids.asStateFlow()

    fun addFavorite(userUuid: String, popupUuid: String) {
        viewModelScope.launch {
            try {
                val request = FavoriteRequest(userUuid, popupUuid)
                RetrofitInstance.favoriteApi.addFavorite(request)
                getFavoriteUserCheck(userUuid)
            } catch (e: Exception) {

            }
        }
    }

    fun deleteFavorite(userUuid: String, popupUuid: String) {
        viewModelScope.launch {
            try {
                val request = FavoriteRequest(userUuid, popupUuid)
                RetrofitInstance.favoriteApi.deleteFavorite(request)
                getFavoriteUserCheck(userUuid)
            } catch (e: Exception) {

            }
        }
    }

    fun getFavoriteUserCheck(userUuid: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.favoriteApi.getFavoriteUserCheck(userUuid)
                val popupUuids = response.map { it.popupUuid }
                _favoritePopupUuids.value = popupUuids
            } catch (e: Exception) {
                 _favoritePopupUuids.value = emptyList()
            }
        }
    }



    fun getFavoriteCount(popupUuid: String, onResult: (Double) -> Unit) {
        viewModelScope.launch {
            try {
                delay(300)
                val response = RetrofitInstance.favoriteApi.getFavoriteCheck(popupUuid)
                val count = response.count ?: 0.0
                onResult(count)
            } catch (e: Exception) {
                onResult(0.0)
            }
        }
    }
}