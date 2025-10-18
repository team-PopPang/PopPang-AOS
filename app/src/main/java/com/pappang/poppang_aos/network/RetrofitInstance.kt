package com.pappang.poppang_aos.network

import com.pappang.poppang_aos.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val AUTH_BASE_URL = BuildConfig.AUTH_BASE_URL
    private const val USER_BASE_URL = BuildConfig.USER_BASE_URL
    private const val BASE_URL = BuildConfig.BASE_URL
    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    val userSignUpApi: UserSignUpApi by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserSignUpApi::class.java)
    }

    val duplicateNicknameApi: DuplicateNicknameApi by lazy {
        Retrofit.Builder()
            .baseUrl(USER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DuplicateNicknameApi::class.java)
    }

    val categoryItemApi: CategoryItemApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CategoryItemApi::class.java)
    }
}