package com.poppang.PopPang.network

import com.poppang.PopPang.model.CategoryItem
import retrofit2.http.GET

interface CategoryItemApi {
    @GET("recommend")
    suspend fun getCategoryItems(): List<CategoryItem>
}
