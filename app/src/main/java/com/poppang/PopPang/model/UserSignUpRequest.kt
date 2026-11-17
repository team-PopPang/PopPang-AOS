package com.poppang.PopPang.model

data class UserSignUpRequest(
    val nickname: String,
    val isAlerted: Boolean = true,
    val fcmToken: String?,
    val alertKeywordList: List<String>,
    val recommendList: List<Long>,
    val signupOs: String = "ANDROID",
)
