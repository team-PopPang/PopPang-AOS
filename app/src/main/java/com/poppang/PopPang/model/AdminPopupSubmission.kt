package com.poppang.PopPang.model

import com.google.gson.annotations.SerializedName

data class AdminPopupSubmissionListItem(
    @SerializedName(
        value = "popupSubmissionId",
        alternate = ["popup_submission_id", "submissionId"]
    )
    val popupSubmissionId: Long? = null,
    @SerializedName(value = "popupUuid", alternate = ["popup_uuid"])
    val popupUuid: String? = null,
    @SerializedName(value = "name", alternate = ["popupName"])
    val name: String,
    @SerializedName(value = "roadAddress", alternate = ["road_address"])
    val roadAddress: String,
    val region: String,
    @SerializedName(value = "submitterUserUuid", alternate = ["submitter_user_uuid"])
    val submitterUserUuid: String,
    @SerializedName(
        value = "submitterNickname",
        alternate = ["submitter", "submitter_nickname"]
    )
    val submitterNickname: String,
    @SerializedName(value = "submittedAt", alternate = ["submitted_at", "submissionDate"])
    val submittedAt: String,
    val status: String
)

data class AdminPopupSubmissionDetail(
    @SerializedName(value = "popupSubmissionId", alternate = ["popup_submission_id"])
    val popupSubmissionId: Long? = null,
    @SerializedName(value = "popupUuid", alternate = ["popup_uuid"])
    val popupUuid: String? = null,
    val name: String,
    val startDate: String,
    val endDate: String,
    val roadAddress: String,
    val region: String,
    val description: String,
    val recommendIdList: List<Long> = emptyList(),
    val recommendList: List<AdminPopupSubmissionRecommend> = emptyList(),
    val imageList: List<AdminPopupSubmissionImage> = emptyList(),
    val address: String? = null,
    val openTime: PopupSubmissionTime? = null,
    val closeTime: PopupSubmissionTime? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val instaPostUrl: String? = null,
    val status: String
)

data class AdminPopupSubmissionRecommend(
    val recommendId: Long,
    val recommendName: String
)

data class AdminPopupSubmissionImage(
    val imageUrl: String,
    val sortOrder: Int
)

data class AdminPopupSubmissionUpdateRequest(
    val status: String,
    val name: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val roadAddress: String? = null,
    val region: String? = null,
    val address: String? = null,
    val openTime: PopupSubmissionTime? = null,
    val closeTime: PopupSubmissionTime? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    val instaPostUrl: String? = null,
    val imageList: List<PopupSubmissionImage>? = null,
    val recommendIdList: List<Long>? = null
)

data class AdminPopupSubmissionUpdateResponse(
    val popupUuid: String?
)
