package com.poppang.PopPang.model

data class KeywordSelectResponse(
    val alertKeyword: String
)

data class KeywordInsertRequest(
    val userUuid: String,
    val newAlertKeyword: String
)

data class KeywordDeleteRequest(
    val userUuid: String,
    val deleteAlertKeyword: String
)