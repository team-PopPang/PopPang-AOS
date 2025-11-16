package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.model.CategoryItem
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoryItemApi{
    @GET(BuildConfig.CATEGORY_ITEM_API)
    suspend fun getCategoryItems(@Query("category") category: String): List<CategoryItem>
}