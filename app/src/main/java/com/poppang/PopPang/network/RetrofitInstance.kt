package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val AUTH_BASE_URL = BuildConfig.AUTH_BASE_URL
    private const val USER_BASE_URL = BuildConfig.USER_BASE_URL
    private const val USERS_BASE_URL = BuildConfig.USERS_BASE_URL
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

    val popupProgressApi: PopupProgressApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PopupProgressApi::class.java)
    }

    val popupComingApi: PopupComingApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PopupComingApi::class.java)
    }

    val popupApi: PopupApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PopupApi::class.java)
    }
    val searchApi: SearchApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SearchApi::class.java)
    }

    val keywordApi: KeywordApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KeywordApi::class.java)
    }

    val favoriteApi: FavoriteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FavoriteApi::class.java)
    }

    val viewCountApi: ViewCountApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ViewCountApi::class.java)
    }

    val fcmTokenApi: FcmTokenApi by lazy {
        Retrofit.Builder()
            .baseUrl(USER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmTokenApi::class.java)
    }

    val alertApi: AlertApi by lazy {
        Retrofit.Builder()
            .baseUrl(USER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AlertApi::class.java)
    }

    val regionsApi: RegionsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegionsApi::class.java)
    }

    val userDrawApi: UserDrawApi by lazy {
        Retrofit.Builder()
            .baseUrl(USER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserDrawApi::class.java)
    }

    val changeNicknameApi : ChangeNicknameApi by lazy {
        Retrofit.Builder()
            .baseUrl(USER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChangeNicknameApi::class.java)
    }

    val userDataApi : UserDataApi by lazy {
        Retrofit.Builder()
            .baseUrl(USER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserDataApi::class.java)
    }

    val recommendPopupApi : RecommendPopupApi by lazy {
        Retrofit.Builder()
            .baseUrl(USERS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecommendPopupApi::class.java)
    }

    val homePopupFilterApi : HomePopuofilterApi by lazy {
        Retrofit.Builder()
            .baseUrl(USERS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HomePopuofilterApi::class.java)
    }

    val alertPopupApi : AlertPopupApi by lazy {
        Retrofit.Builder()
            .baseUrl(USERS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AlertPopupApi::class.java)
    }

}