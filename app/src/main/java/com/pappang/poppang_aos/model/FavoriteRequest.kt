package com.pappang.poppang_aos.model

data class FavoriteRequest(
    val userUuid: String,
    val popupUuid: String
)

data class FavoriteCountResponse(
    val count: Double
)