package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.FavoriteRequest
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST

interface FavoriteApi {
    @POST(BuildConfig.V2_FAVORITE_PATH)
    suspend fun addFavorite(@Body request: FavoriteRequest)
    @HTTP(method = "DELETE", path = BuildConfig.V2_FAVORITE_PATH, hasBody = true)
    suspend fun deleteFavorite(@Body request: FavoriteRequest)

    @GET(BuildConfig.V2_FAVORITE_POPUPS_PATH)
    suspend fun getFavoriteUserCheck(): ApiResponse<List<PopupEvent>>
}
