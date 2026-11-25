package com.poppang.PopPang.viewmodel

import androidx.lifecycle.ViewModel
import com.poppang.PopPang.model.CategoryItem
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.network.MapPopupfilterApi
import com.poppang.PopPang.network.RetrofitInstance
import java.io.IOException
import retrofit2.HttpException

internal data class MapArea(
    val region: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val sort: String,
)

class MapPopupfilterViewModel(
    private val api: MapPopupfilterApi = RetrofitInstance.mapPopupFilterApi,
) : ViewModel() {
    internal suspend fun categories(): List<CategoryItem> = load { api.getMapCategories() }.orEmpty()

    internal suspend fun popups(
        area: MapArea,
        recommendIds: List<Long> = emptyList(),
        query: String = "",
    ): List<PopupEvent>? = load {
        api.getMapPopupfilter(
            area.region,
            area.district,
            area.latitude,
            area.longitude,
            area.sort,
            recommendIds,
            query.trim().ifEmpty { null },
        )
    }

    private suspend fun <T> load(request: suspend () -> List<T>): List<T>? =
        try {
            request()
        } catch (_: IOException) {
            null
        } catch (_: HttpException) {
            null
        }
}
