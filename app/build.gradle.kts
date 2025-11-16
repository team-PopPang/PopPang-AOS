import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.poppang.PopPang"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.poppang.PopPang"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        localProperties.load(project.rootProject.file("local.properties").inputStream())
        val kakaoKey = localProperties.getProperty("KAKAO_KEY") ?: ""
        val googleKey = localProperties.getProperty("GOOGLE_KEY") ?: ""
        val authurl = localProperties.getProperty("AUTH_BASE_URL") ?: ""
        val userurl = localProperties.getProperty("USER_BASE_URL") ?: ""
        val usersurl = localProperties.getProperty("USERS_BASE_URL") ?: ""
        val baseurl = localProperties.getProperty("BASE_URL") ?: ""
        val urlimage = localProperties.getProperty("URL_IMAGE") ?: ""
        val authapikakao = localProperties.getProperty("AUTH_API_KAKAO") ?: ""
        val authapigoogle = localProperties.getProperty("AUTH_API_GOOGLE") ?: ""
        val authapiautologin = localProperties.getProperty("AUTH_API_AUTOLOGIN") ?: ""
        val duplicateapi = localProperties.getProperty("DUPLICATE_API") ?: ""
        val signupapikakao = localProperties.getProperty("SIGNUP_API_KAKAO") ?: ""
        val signupapigoogle = localProperties.getProperty("SIGNUP_API_GOOGLE") ?: ""
        val catagoryitemapi = localProperties.getProperty("CATEGORY_ITEM_API") ?: ""
        val popupprogressapi = localProperties.getProperty("POPUP_PROGRESS_API") ?: ""
        val popupcomingapi = localProperties.getProperty("POPUP_COMING_API") ?: ""
        val popupapi = localProperties.getProperty("POPUP_API") ?: ""
        val searchapi = localProperties.getProperty("SEARCH_API") ?: ""
        val keywordapi = localProperties.getProperty("KEYWORD_API") ?: ""
        val navermapkey = localProperties.getProperty("NAVER_MAP_KEY") ?: ""
        val favoriteapi = localProperties.getProperty("FAVORITE_API") ?: ""
        val favoriteusercheckapi = localProperties.getProperty("FAVORITE_USER_CHECK_API") ?: ""
        val favoritecheckapi = localProperties.getProperty("FAVORITE_CHECK_API") ?: ""
        val viewcountapi = localProperties.getProperty("VIEW_COUNT_API") ?: ""
        val viewcounttotalapi = localProperties.getProperty("VIEW_COUNT_TOTAL_API") ?: ""
        val fcmapi = localProperties.getProperty("FCM_API") ?: ""
        val alertapi = localProperties.getProperty("ALERT_API") ?: ""
        val regionsapi = localProperties.getProperty("REGIONS_API") ?: ""
        val userapi = localProperties.getProperty("USER_API") ?: ""
        val userwithdrawapi = localProperties.getProperty("USER_WITHDRAW_API") ?: ""
        val recommendpopupapi = localProperties.getProperty("RECOMMEND_POPUP_API") ?: ""
        buildConfigField("String", "KAKAO_KEY", "\"$kakaoKey\"")
        buildConfigField("String", "GOOGLE_KEY", "\"$googleKey\"")
        manifestPlaceholders["KAKAO_KEY"] = kakaoKey
        buildConfigField("String", "AUTH_BASE_URL", "\"$authurl\"")
        buildConfigField("String", "USER_BASE_URL", "\"$userurl\"")
        buildConfigField("String", "USERS_BASE_URL", "\"$usersurl\"")
        buildConfigField("String", "BASE_URL", "\"$baseurl\"")
        buildConfigField("String", "AUTH_API_KAKAO", "\"$authapikakao\"")
        buildConfigField("String", "AUTH_API_GOOGLE", "\"$authapigoogle\"")
        buildConfigField("String", "AUTH_API_AUTOLOGIN", "\"$authapiautologin\"")
        buildConfigField("String", "DUPLICATE_API", "\"$duplicateapi\"")
        buildConfigField("String", "SIGNUP_API_KAKAO", "\"$signupapikakao\"")
        buildConfigField("String", "SIGNUP_API_GOOGLE", "\"$signupapigoogle\"")
        buildConfigField("String", "CATEGORY_ITEM_API", "\"$catagoryitemapi\"")
        buildConfigField("String", "POPUP_PROGRESS_API", "\"$popupprogressapi\"")
        buildConfigField("String", "URL_IMAGE", "\"$urlimage\"")
        buildConfigField("String", "POPUP_COMING_API", "\"$popupcomingapi\"")
        buildConfigField("String", "POPUP_API", "\"$popupapi\"")
        buildConfigField("String", "SEARCH_API", "\"$searchapi\"")
        buildConfigField("String", "KEYWORD_API", "\"$keywordapi\"")
        manifestPlaceholders["NAVER_MAP_KEY"] = navermapkey
        buildConfigField("String", "FAVORITE_API", "\"$favoriteapi\"")
        buildConfigField("String", "FAVORITE_USER_CHECK_API", "\"$favoriteusercheckapi\"")
        buildConfigField("String", "FAVORITE_CHECK_API", "\"$favoritecheckapi\"")
        buildConfigField("String", "VIEW_COUNT_API", "\"$viewcountapi\"")
        buildConfigField("String", "VIEW_COUNT_TOTAL_API", "\"$viewcounttotalapi\"")
        buildConfigField("String", "FCM_API", "\"$fcmapi\"")
        buildConfigField("String", "ALERT_API", "\"$alertapi\"")
        buildConfigField("String", "REGIONS_API", "\"$regionsapi\"")
        buildConfigField("String", "USER_WITHDRAW_API", "\"$userwithdrawapi\"")
        buildConfigField("String", "USER_API", "\"$userapi\"")
        buildConfigField("String", "RECOMMEND_POPUP_API", "\"$recommendpopupapi\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf("-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.v2.user)
    implementation(libs.play.services.auth)
    implementation(libs.google.services)
    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.naver.map.compose)
    implementation (libs.play.services.location)
    implementation (libs.naver.map.location)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}