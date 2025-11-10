package com.pappang.poppang_aos.network

import com.pappang.poppang_aos.BuildConfig
import com.pappang.poppang_aos.model.FavoriteCountResponse
import com.pappang.poppang_aos.model.FavoriteRequest
import com.pappang.poppang_aos.model.PopupEvent
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteApi {
    @POST(BuildConfig.FAVORITE_API)
    suspend fun addFavorite(@Body request: FavoriteRequest)

    @HTTP(method = "DELETE", path = BuildConfig.FAVORITE_API, hasBody = true)
    suspend fun deleteFavorite(@Body request: FavoriteRequest)

    @GET(BuildConfig.FAVORITE_USER_CHECK_API)
    suspend fun getFavoriteUserCheck(@Path("userUuid") userUuid: String) : List<PopupEvent>

    @GET(BuildConfig.FAVORITE_CHECK_API)
    suspend fun getFavoriteCheck(@Path("popupUuid") popupUuid: String) : FavoriteCountResponse
}