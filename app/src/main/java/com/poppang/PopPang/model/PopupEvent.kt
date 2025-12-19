package com.poppang.PopPang.model

import com.poppang.PopPang.BuildConfig

data class PopupEvent(
    val popupUuid: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val openTime: String,
    val closeTime: String,
    val address: String,
    val roadAddress: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val instaPostId: String,
    val instaPostUrl: String,
    val captionSummary: String,
    val imageUrlList: List<String>,
    val mediaType: String,
    val recommendList: List<String>,
    val favoriteCount: Double,
    val viewCount: Double,
    val isFavorited: Boolean,
    val isRead: Boolean
){
    val fullImageUrlList: List<String>
        get() = imageUrlList.map { BuildConfig.URL_IMAGE + it }
    val startDateFormatted: String
        get() = startDate.takeIf { it.length >= 10 }?.let { "${it.substring(2,4)}.${it.substring(5,7)}.${it.substring(8,10)}" } ?: ""
    val endDateFormatted: String
        get() = endDate.takeIf { it.length >= 10 }?.let { "${it.substring(2,4)}.${it.substring(5,7)}.${it.substring(8,10)}" } ?: ""
}



