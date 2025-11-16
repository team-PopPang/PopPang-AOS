package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel

class AddKeywordViewModel : ViewModel() {
    val keywordList: SnapshotStateList<String> = mutableStateListOf()

    fun addKeyword(keyword: String) {
        val text = keyword.trim()
        if (text.isNotEmpty() && !keywordList.contains(text) && keywordList.size < 3) {
            keywordList.add(text)
       }
    }

    fun removeKeyword(keyword: String) {
        keywordList.remove(keyword)
    }
}