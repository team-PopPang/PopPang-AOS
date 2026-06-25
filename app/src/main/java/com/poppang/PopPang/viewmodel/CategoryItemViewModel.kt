package com.poppang.PopPang.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poppang.PopPang.model.CategoryItem
import com.poppang.PopPang.network.RetrofitInstance
import kotlinx.coroutines.launch

class CategoryItemViewModel : ViewModel() {
    val categories: SnapshotStateList<CategoryItem> = mutableStateListOf()
    val selectedCategories: SnapshotStateList<CategoryItem> = mutableStateListOf()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var isLoaded = false

    fun fetchCategoryItems(forceRefresh: Boolean = false) {
        if (isLoading || (!forceRefresh && isLoaded)) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = RetrofitInstance.categoryItemApi.getCategoryItems("")
                categories.clear()
                categories.addAll(result)
                isLoaded = true
            } catch (e: Exception) {
                errorMessage = "카테고리를 불러오지 못했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    fun retryFetchCategoryItems() {
        fetchCategoryItems(forceRefresh = true)
    }

    fun toggleCategory(category: CategoryItem) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category)
        } else {
            selectedCategories.add(category)
        }
    }
}
