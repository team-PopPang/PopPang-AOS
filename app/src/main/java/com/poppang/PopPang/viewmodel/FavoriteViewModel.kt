package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.FavoriteRequest
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.FavoriteApi
import com.poppang.PopPang.network.FavoriteCountApi
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val api: FavoriteApi = RetrofitInstance.favoriteApi,
    private val countApi: FavoriteCountApi = RetrofitInstance.favoriteCountApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel() {
    private val _favoritePopupUuids = MutableStateFlow<List<String>>(emptyList())
    val favoritePopupUuids: StateFlow<List<String>> = _favoritePopupUuids.asStateFlow()
    private val _favoriteCounts = MutableStateFlow<Map<String, Long>>(emptyMap())
    val favoriteCounts: StateFlow<Map<String, Long>> = _favoriteCounts.asStateFlow()

    fun updateCountsFromList(popups: List<PopupEvent>) {
        _favoriteCounts.value = _favoriteCounts.value + popups.associate { it.popupUuid to it.favoriteCount }
    }

    fun addFavorite(userUuid: String, popupUuid: String) {
        viewModelScope.launch(dispatcher) {
            try {
                val request = FavoriteRequest(popupUuid)
                api.addFavorite(request)
                getFavoriteUserCheck(userUuid)
                getFavoriteCount(userUuid, popupUuid) {}
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep the last confirmed counts if the write fails.
            }
        }
    }

    fun deleteFavorite(userUuid: String, popupUuid: String) {
        viewModelScope.launch(dispatcher) {
            try {
                val request = FavoriteRequest(popupUuid)
                api.deleteFavorite(request)
                getFavoriteUserCheck(userUuid)
                getFavoriteCount(userUuid, popupUuid) {}
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep the last confirmed counts if the write fails.
            }
        }
    }

    fun getFavoriteUserCheck(userUuid: String) {
        viewModelScope.launch(dispatcher) {
            try {
                val response = countApi.getFavoriteUserCheck().requireData()
                val popupUuids = response
                    .filter { it.isFavorited == true }
                    .map { it.popupUuid }
                _favoritePopupUuids.value = popupUuids
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Preserve the last confirmed favorites when a refresh fails.
            }
        }
    }

    fun getFavoriteCount(userUuid: String, popupUuid: String, onResult: (Long) -> Unit) {
        viewModelScope.launch(dispatcher) {
            try {
                val response = countApi.getFavoriteCheck(popupUuid).requireData()
                val count = response.count
                _favoriteCounts.value = _favoriteCounts.value + (popupUuid to count)
                onResult(count)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _favoriteCounts.value[popupUuid]?.let(onResult)
            }
        }
    }
}
