package com.pappang.poppang_aos.model

import com.pappang.poppang_aos.BuildConfig

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
    val recommend: String
){
    val fullImageUrlList: List<String>
        get() = imageUrlList.map { BuildConfig.URL_IMAGE + it }
    val startDateFormatted: String
        get() = startDate?.replace("-", ".") ?: ""
    val endDateFormatted: String
        get() = endDate?.replace("-", ".") ?: ""
}
