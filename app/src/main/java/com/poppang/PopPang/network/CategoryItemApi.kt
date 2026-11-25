package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.CategoryItem
import retrofit2.http.GET

interface CategoryItemApi {
    @GET(BuildConfig.V2_CATEGORIES_PATH)
    suspend fun getCategoryItems(): List<CategoryItem>
}
