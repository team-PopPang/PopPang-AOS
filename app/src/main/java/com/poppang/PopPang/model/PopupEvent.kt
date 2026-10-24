package com.poppang.PopPang.model

import com.poppang.PopPang.BuildConfig
import com.google.gson.annotations.SerializedName

data class PopupEvent(
    val popupUuid: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    @SerializedName("openTime") private val openingTime: String?,
    @SerializedName("closeTime") private val closingTime: String?,
    val address: String,
    @SerializedName("roadAddress") private val roadAddressValue: String?,
    val region: String,
    val latitude: Double?,
    val longitude: Double?,
    val instaPostId: String?,
    val instaPostUrl: String?,
    val captionSummary: String,
    val imageUrlList: List<String>,
    val mediaType: String?,
    @SerializedName("recommendList") private val recommendations: List<String>?,
    val favoriteCount: Long,
    val viewCount: Long,
    val isFavorited: Boolean,
    val isRead: Boolean = false,
){
    val openTime: String get() = openingTime.orEmpty()
    val closeTime: String get() = closingTime.orEmpty()
    val roadAddress: String get() = roadAddressValue?.takeIf(String::isNotBlank) ?: address
    val recommendList: List<String> get() = recommendations.orEmpty()
    val fullImageUrlList: List<String>
        get() = imageUrlList.map { popupImageUrl(BuildConfig.URL_IMAGE, it) }
    val startDateFormatted: String
        get() = startDate.takeIf { it.length >= 10 }?.let { "${it.substring(2,4)}.${it.substring(5,7)}.${it.substring(8,10)}" } ?: ""
    val endDateFormatted: String
        get() = endDate.takeIf { it.length >= 10 }?.let { "${it.substring(2,4)}.${it.substring(5,7)}.${it.substring(8,10)}" } ?: ""
}

internal fun popupImageUrl(baseUrl: String, imageUrl: String): String =
    if (imageUrl.startsWith("https://") || imageUrl.startsWith("http://")) {
        imageUrl
    } else {
        baseUrl.trimEnd('/') + "/" + imageUrl.trimStart('/')
    }



