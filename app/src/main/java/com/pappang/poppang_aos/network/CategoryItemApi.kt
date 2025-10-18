package com.pappang.poppang_aos.network

import com.pappang.poppang_aos.BuildConfig
import com.pappang.poppang_aos.model.CategoryItem
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoryItemApi{
    @GET(BuildConfig.CATEGORY_ITEM_API)
    suspend fun getCategoryItems(@Query("category") category: String): List<CategoryItem>
}