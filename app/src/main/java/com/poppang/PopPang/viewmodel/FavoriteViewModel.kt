package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.FavoriteRequest
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.FavoriteApi
import com.poppang.PopPang.network.RetrofitInstance
import com.poppang.PopPang.network.SelectPopupApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val api: FavoriteApi = RetrofitInstance.favoriteApi,
    private val detailApi: SelectPopupApi = RetrofitInstance.selectPopupApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel() {
    private val _favoritePopupUuids = MutableStateFlow<List<String>>(emptyList())
    val favoritePopupUuids: StateFlow<List<String>> = _favoritePopupUuids.asStateFlow()
    private val _favoriteCounts = MutableStateFlow<Map<String, Long>>(emptyMap())
    val favoriteCounts: StateFlow<Map<String, Long>> = _favoriteCounts.asStateFlow()

    fun updateCountsFromList(popups: List<PopupEvent>) {
        _favoriteCounts.value = _favoriteCounts.value + popups.associate { it.popupUuid to it.favoriteCount }
    }

    private val pendingWrites = mutableSetOf<String>()
    private var favoriteListVersion = 0L
    var hasFavoriteList by mutableStateOf(false)
        private set

    fun addFavorite(userUuid: String, popupUuid: String) = setFavorite(userUuid, popupUuid, true)

    fun deleteFavorite(userUuid: String, popupUuid: String) = setFavorite(userUuid, popupUuid, false)

    private fun setFavorite(userUuid: String, popupUuid: String, liked: Boolean) {
        viewModelScope.launch(dispatcher) {
            if (!pendingWrites.add(popupUuid)) return@launch
            try {
                val request = FavoriteRequest(popupUuid)
                if (liked) api.addFavorite(request) else api.deleteFavorite(request)
                _favoritePopupUuids.value = if (liked) {
                    (_favoritePopupUuids.value + popupUuid).distinct()
                } else {
                    _favoritePopupUuids.value - popupUuid
                }
                getFavoriteUserCheck(userUuid)
                // The favorite list no longer contains a removed popup. Its detail carries the confirmed count.
                if (!liked) updateCountsFromList(listOf(detailApi.getSelectPopup(popupUuid)))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep the last server count until the next list or detail response.
            } finally {
                pendingWrites.remove(popupUuid)
            }
        }
    }

    fun getFavoriteUserCheck(userUuid: String) {
        val version = ++favoriteListVersion
        viewModelScope.launch(dispatcher) {
            try {
                val response = api.getFavoriteUserCheck().requireData()
                if (version != favoriteListVersion) return@launch
                updateCountsFromList(response)
                val popupUuids = response
                    .filter { it.isFavorited == true }
                    .map { it.popupUuid }
                _favoritePopupUuids.value = popupUuids
                hasFavoriteList = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Preserve the last confirmed favorites when a refresh fails.
            }
        }
    }
}
