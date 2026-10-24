package com.poppang.PopPang.network

import com.poppang.PopPang.model.ApiResponse
import com.poppang.PopPang.model.FavoriteCountResponse
import com.poppang.PopPang.model.FavoriteRequest
import com.poppang.PopPang.model.PopupEvent
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteApi {
    @POST("favorite")
    suspend fun addFavorite(@Body request: FavoriteRequest)
    @HTTP(method = "DELETE", path = "favorite", hasBody = true)
    suspend fun deleteFavorite(@Body request: FavoriteRequest)
}

interface FavoriteCountApi {
    @GET("favorite/popup")
    suspend fun getFavoriteUserCheck(): ApiResponse<List<PopupEvent>>
    @GET("favorite/count/{popupUuid}")
    suspend fun getFavoriteCheck(@Path("popupUuid") popupUuid: String): ApiResponse<FavoriteCountResponse>
}
