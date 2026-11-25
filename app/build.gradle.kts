import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.poppang.PopPang"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.poppang.PopPang"
        minSdk = 28
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 27
        versionName = "1.2.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val endpointDefaults = Properties().apply {
            rootProject.file("api-endpoints.properties").inputStream().use { load(it) }
        }
        val localProperties = Properties(endpointDefaults).apply {
            rootProject.file("local.properties").takeIf { it.isFile }?.inputStream()?.use { load(it) }
        }
        val kakaoKey = localProperties.getProperty("KAKAO_KEY") ?: ""
        val googleKey = localProperties.getProperty("GOOGLE_KEY") ?: ""
        val admobKey = localProperties.getProperty("ADMOB_KEY")
            ?: "ca-app-pub-3940256099942544~3347511713"
        val admobNativeKey = localProperties.getProperty("ADMOB_NATIVE_KEY")
            ?: "ca-app-pub-3940256099942544/2247696110"
        val adminbaseurl = localProperties.getProperty("ADMIN_BASE_URL") ?: ""
        val v2BaseUrl = localProperties.getProperty("V2_BASE_URL").trimEnd('/') + "/"
        val baseUri = URI(v2BaseUrl)
        require(baseUri.scheme in setOf("http", "https") && baseUri.host != null && baseUri.query == null && baseUri.fragment == null) {
            "V2_BASE_URL must be an HTTP(S) base URL without a query or fragment"
        }
        val urlimage = localProperties.getProperty("URL_IMAGE") ?: ""
        val navermapkey = localProperties.getProperty("NAVER_MAP_KEY") ?: ""
        val popupdeactivateapi = localProperties.getProperty("POPUP_DEACTIVATE_API") ?: ""
        buildConfigField("String", "KAKAO_KEY", "\"$kakaoKey\"")
        buildConfigField("String", "GOOGLE_KEY", "\"$googleKey\"")
        buildConfigField("String", "ADMOB_KEY", "\"$admobKey\"")
        buildConfigField("String", "ADMOB_NATIVE_KEY", "\"$admobNativeKey\"")
        manifestPlaceholders["KAKAO_KEY"] = kakaoKey
        manifestPlaceholders["ADMOB_KEY"] = admobKey
        buildConfigField("String", "ADMIN_BASE_URL", "\"$adminbaseurl\"")
        buildConfigField("String", "V2_BASE_URL", "\"$v2BaseUrl\"")
        val placeholder = Regex("""\{[a-zA-Z][a-zA-Z0-9_-]*\}""")
        fun placeholders(path: String) = placeholder.findAll(path).map { it.value }.toSet()
        for (key in endpointDefaults.stringPropertyNames().filter { it.endsWith("_PATH") }.sorted()) {
            val path = localProperties.getProperty(key)
            require(placeholders(path) == placeholders(endpointDefaults.getProperty(key))) {
                "$key must keep the path parameters from api-endpoints.properties"
            }
            val uri = URI(placeholder.replace(path, "id"))
            require(path.isNotBlank() && !path.startsWith("/") && !uri.isAbsolute &&
                uri.query == null && uri.fragment == null && uri.path.split('/').none { it == "." || it == ".." }) {
                "$key must be a relative API path without a query or fragment"
            }
            buildConfigField("String", key, "\"$path\"")
        }
        buildConfigField("String", "URL_IMAGE", "\"$urlimage\"")
        manifestPlaceholders["NAVER_MAP_KEY"] = navermapkey
        buildConfigField("String", "POPUP_DEACTIVATE_API", "\"$popupdeactivateapi\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            freeCompilerArgs.add("-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.poppang.rn.android)
    implementation(libs.poppang.rn.android.compose)
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
    implementation(libs.play.services.ads)
    implementation(libs.google.services)
    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.naver.map.compose)
    implementation (libs.play.services.location)
    implementation (libs.naver.map.location)
    implementation(libs.firebase.messaging)
    implementation(libs.app.update.ktx)
    implementation(libs.v2.share)
    implementation("io.morfly.compose:advanced-bottomsheet-material3-android:0.1.0")
    testImplementation(libs.junit)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
