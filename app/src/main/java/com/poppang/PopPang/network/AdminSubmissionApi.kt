package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.AdminPopupSubmissionDetail
import com.poppang.PopPang.model.AdminPopupSubmissionListItem
import com.poppang.PopPang.model.AdminPopupSubmissionUpdateRequest
import com.poppang.PopPang.model.AdminPopupSubmissionUpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface AdminSubmissionApi {
    @GET(BuildConfig.POPUP_SUBMISSION_API)
    suspend fun getAdminPopupSubmissions(
        @Query("uuid") adminUuid: String,
        @Query("status") status: String
    ): List<AdminPopupSubmissionListItem>

    @GET(BuildConfig.POPUP_SUBMISSION_API)
    suspend fun getAdminPopupSubmissionDetail(
        @Query("popup_uuid") popupUuid: String,
        @Query("user_uuid") adminUuid: String
    ): AdminPopupSubmissionDetail

    @GET("${BuildConfig.POPUP_SUBMISSION_API}/{popupSubmissionId}")
    suspend fun getAdminPopupSubmissionDetailById(
        @Path("popupSubmissionId") popupSubmissionId: Long,
        @Query("uuid") adminUuid: String
    ): AdminPopupSubmissionDetail

    @PUT(BuildConfig.POPUP_SUBMISSION_API)
    suspend fun updateAdminPopupSubmission(
        @Query("popup_uuid") popupUuid: String,
        @Query("user_uuid") adminUuid: String,
        @Body request: AdminPopupSubmissionUpdateRequest
    ): Response<AdminPopupSubmissionUpdateResponse>

    @PUT("${BuildConfig.POPUP_SUBMISSION_API}/{popupSubmissionId}")
    suspend fun updateAdminPopupSubmissionById(
        @Path("popupSubmissionId") popupSubmissionId: Long,
        @Query("uuid") adminUuid: String,
        @Body request: AdminPopupSubmissionUpdateRequest
    ): Response<AdminPopupSubmissionUpdateResponse>
}
