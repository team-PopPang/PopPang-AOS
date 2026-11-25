package com.poppang.PopPang.network

import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.datastore.AuthTokenStore
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val publicClient = OkHttpClient.Builder().retryOnConnectionFailure(false).build()
    private val accessClient =
        publicClient
            .newBuilder()
            .addInterceptor(SessionInterceptor({ AuthTokenStore.session }))
            .build()
    private val signupClient =
        publicClient
            .newBuilder()
            .addInterceptor(SessionInterceptor({ AuthTokenStore.session }, signup = true))
            .build()

    private fun retrofit(
        baseUrl: String,
        client: OkHttpClient,
    ) = Retrofit
        .Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    internal fun mobileBaseUrl(v2BaseUrl: String) =
        v2BaseUrl.trimEnd('/') + "/" + BuildConfig.V2_MOBILE_PATH.trimEnd('/') + "/"

    private val mobileUrl by lazy { mobileBaseUrl(BuildConfig.V2_BASE_URL) }
    private val publicV2 by lazy { retrofit(mobileUrl, publicClient) }
    private val accessV2 by lazy { retrofit(mobileUrl, accessClient) }
    private val signupV2 by lazy { retrofit(mobileUrl, signupClient) }
    private val admin by lazy { retrofit(BuildConfig.ADMIN_BASE_URL, publicClient) }

    val authApi: AuthApi by lazy { publicV2.create(AuthApi::class.java) }
    val logoutApi: LogoutApi by lazy { accessV2.create(LogoutApi::class.java) }
    val userSignUpApi: UserSignUpApi by lazy { signupV2.create(UserSignUpApi::class.java) }
    val duplicateNicknameApi: DuplicateNicknameApi by lazy { accessV2.create(DuplicateNicknameApi::class.java) }
    val signupNicknameApi: DuplicateNicknameApi by lazy { signupV2.create(DuplicateNicknameApi::class.java) }
    val suggestionApi: SignupSuggestionApi by lazy { accessV2.create(SignupSuggestionApi::class.java) }
    val signupSuggestionApi: SignupSuggestionApi by lazy { signupV2.create(SignupSuggestionApi::class.java) }
    val categoryItemApi: CategoryItemApi by lazy { accessV2.create(CategoryItemApi::class.java) }
    val signupCategoryApi: CategoryItemApi by lazy { signupV2.create(CategoryItemApi::class.java) }
    val adminPopupApi: AdminPopupApi by lazy { admin.create(AdminPopupApi::class.java) }
    val popupComingApi: PopupComingApi by lazy { accessV2.create(PopupComingApi::class.java) }
    val popupApi: PopupApi by lazy { accessV2.create(PopupApi::class.java) }
    val searchApi: SearchApi by lazy { accessV2.create(SearchApi::class.java) }
    val keywordApi: KeywordApi by lazy { accessV2.create(KeywordApi::class.java) }
    val favoriteApi: FavoriteApi by lazy { accessV2.create(FavoriteApi::class.java) }
    val viewCountIncrementApi: ViewCountIncrementApi by lazy { accessV2.create(ViewCountIncrementApi::class.java) }
    val fcmTokenApi: FcmTokenApi by lazy { accessV2.create(FcmTokenApi::class.java) }
    val alertApi: AlertApi by lazy { accessV2.create(AlertApi::class.java) }
    val regionsApi: RegionsApi by lazy { accessV2.create(RegionsApi::class.java) }
    val userDrawApi: UserDrawApi by lazy { accessV2.create(UserDrawApi::class.java) }
    val changeNicknameApi: ChangeNicknameApi by lazy { accessV2.create(ChangeNicknameApi::class.java) }
    val userDataApi: UserDataApi by lazy { accessV2.create(UserDataApi::class.java) }
    val recommendPopupApi: RecommendPopupApi by lazy {
        // Interest-based home recommendations retain the V2 route pending the product decision.
        retrofit(BuildConfig.V2_BASE_URL, accessClient).create(RecommendPopupApi::class.java)
    }
    val homePopupFilterApi: HomePopupfilterApi by lazy { accessV2.create(HomePopupfilterApi::class.java) }
    val alertPopupApi: AlertPopupApi by lazy { accessV2.create(AlertPopupApi::class.java) }
    val mapPopupFilterApi: MapPopupfilterApi by lazy { accessV2.create(MapPopupfilterApi::class.java) }
    val popupRelatedApi: PopuprelatedApi by lazy { accessV2.create(PopuprelatedApi::class.java) }
    val selectPopupApi: SelectPopupApi by lazy { accessV2.create(SelectPopupApi::class.java) }
}
