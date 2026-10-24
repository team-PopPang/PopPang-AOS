package com.poppang.PopPang.model

data class KeywordSelectResponse(
    val alertKeyword: String
)

data class KeywordInsertRequest(
    val keyword: String
)

data class KeywordDeleteRequest(
    val keyword: String
)
