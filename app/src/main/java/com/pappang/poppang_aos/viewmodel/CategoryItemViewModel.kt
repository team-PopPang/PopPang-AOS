package com.pappang.poppang_aos.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pappang.poppang_aos.model.CategoryItem
import com.pappang.poppang_aos.network.RetrofitInstance
import kotlinx.coroutines.launch

class CategoryItemViewModel : ViewModel() {
    val categories: SnapshotStateList<CategoryItem> = mutableStateListOf()
    val selectedCategories: SnapshotStateList<CategoryItem> = mutableStateListOf()

    fun fetchCategoryItems() {
        viewModelScope.launch {
            try {
                val result = RetrofitInstance.categoryItemApi.getCategoryItems("")
                categories.clear()
                categories.addAll(result)
            } catch (e: Exception) {
            }
        }
    }

    fun toggleCategory(category: CategoryItem) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category)
        } else {
            selectedCategories.add(category)
        }
    }
}