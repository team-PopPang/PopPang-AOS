package com.poppang.PopPang.model

data class FavoriteRequest(
    val popupUuid: String
)

data class FavoriteCountResponse(
    val count: Long
)
