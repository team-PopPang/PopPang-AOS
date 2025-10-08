import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.pappang.poppang_aos"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pappang.poppang_aos"
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
        val authapikakao = localProperties.getProperty("AUTH_API_KAKAO") ?: ""
        val authapigoogle = localProperties.getProperty("AUTH_API_GOOGLE") ?: ""
        val authapiautologin = localProperties.getProperty("AUTH_API_AUTOLOGIN") ?: ""
        val duplicateapi = localProperties.getProperty("DUPLICATE_API") ?: ""
        val signupapikakao = localProperties.getProperty("SIGNUP_API_KAKAO") ?: ""
        val signupapigoogle = localProperties.getProperty("SIGNUP_API_GOOGLE") ?:
        buildConfigField("String", "KAKAO_KEY", "\"$kakaoKey\"")
        buildConfigField("String", "GOOGLE_KEY", "\"$googleKey\"")
        manifestPlaceholders["KAKAO_KEY"] = kakaoKey
        buildConfigField("String", "AUTH_BASE_URL", "\"$authurl\"")
        buildConfigField("String", "USER_BASE_URL", "\"$userurl\"")
        buildConfigField("String", "AUTH_API_KAKAO", "\"$authapikakao\"")
        buildConfigField("String", "AUTH_API_GOOGLE", "\"$authapigoogle\"")
        buildConfigField("String", "AUTH_API_AUTOLOGIN", "\"$authapiautologin\"")
        buildConfigField("String", "DUPLICATE_API", "\"$duplicateapi\"")
        buildConfigField("String", "SIGNUP_API_KAKAO", "\"$signupapikakao\"")
        buildConfigField("String", "SIGNUP_API_GOOGLE", "\"$signupapigoogle\"")
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}