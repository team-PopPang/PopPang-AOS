package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.PopupSubmissionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SubmissionApi {
    @POST(BuildConfig.POPUP_SUBMISSION_API)
    suspend fun createPopupSubmission(
        @Body request: PopupSubmissionRequest
    ): Response<Unit>
}
