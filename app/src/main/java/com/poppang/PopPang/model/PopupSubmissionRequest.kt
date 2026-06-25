package com.poppang.PopPang.model

data class PopupSubmissionRequest(
    val userUuid: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val openTime: PopupSubmissionTime?,
    val closeTime: PopupSubmissionTime?,
    val address: String?,
    val roadAddress: String,
    val region: String,
    val instaPostUrl: String?,
    val description: String,
    val imageList: List<PopupSubmissionImage>,
    val recommendIdList: List<Long>
)

data class PopupSubmissionTime(
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
    val nano: Int = 0
)

data class PopupSubmissionImage(
    val imageUrl: String,
    val sortOrder: Int
)
