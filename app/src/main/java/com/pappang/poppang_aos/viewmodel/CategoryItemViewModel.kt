package com.pappang.poppang_aos.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel

class CategoryItemViewModel : ViewModel() {
    val categories: SnapshotStateList<String> = mutableStateListOf(
        "애니메이션", "캐릭터", "화장품", "패션", "식음료", "테크/가전", "문화/전시", "일상용품/리빙", "엔터테인먼트", "지역/로컬", "콜라보.굿즈"
    )
    val selectedCategories: SnapshotStateList<String> = mutableStateListOf()

    fun toggleCategory(category: String) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category)
        } else {
            selectedCategories.add(category)
        }
    }
}