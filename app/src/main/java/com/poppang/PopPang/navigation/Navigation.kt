package com.poppang.PopPang.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.view.LoginScreen
import com.poppang.PopPang.view.MainScreen
import com.poppang.PopPang.view.OnboardingScreen
import com.poppang.PopPang.view.SignUpScreen
import com.poppang.PopPang.view.SplashScreen
import com.poppang.PopPang.viewmodel.AddKeywordViewModel
import com.poppang.PopPang.viewmodel.AutoLoginViewModel
import com.poppang.PopPang.viewmodel.CategoryItemViewModel
import com.poppang.PopPang.viewmodel.DuplicateNickname
import com.poppang.PopPang.viewmodel.PopupComingViewModel
import com.poppang.PopPang.viewmodel.PopupProgressViewModel
import com.poppang.PopPang.viewmodel.PopupViewModel
import com.poppang.PopPang.viewmodel.RecommendPopupViewModel
import com.poppang.PopPang.viewmodel.UserDataViewModel
import kotlinx.coroutines.delay

@Composable
fun Navigation(
    nicknameViewModel: DuplicateNickname,
    keywordViewModel: AddKeywordViewModel,
    categoryViewModel: CategoryItemViewModel,
    popupprogressViewModel: PopupProgressViewModel,
    popupcomingViewModel: PopupComingViewModel,
    popupViewModel: PopupViewModel,
    recommendPopupViewModel: RecommendPopupViewModel,
    hideSystemBars: (Boolean) -> Unit,
    userDataViewModel: UserDataViewModel = viewModel(),
    autoLoginViewModel: AutoLoginViewModel = viewModel(),
    deepLinkPopupUuid: String? = null
) {
    val navController = rememberNavController()
    var loginResponse by remember { mutableStateOf<LoginResponse?>(null) }
    val latestUserData by userDataViewModel.userData.collectAsState()
    var fcmToken by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("fcm", Context.MODE_PRIVATE)
        fcmToken = prefs.getString("fcm_token", null)
    }

    LaunchedEffect(latestUserData) {
        if (latestUserData != null) {
            loginResponse = latestUserData
        }
        Log.d("Navigation", "Latest user data updated: $latestUserData, loginResponse: $loginResponse")
    }
    Scaffold(
        modifier = Modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen()

                val autoLoginResult by autoLoginViewModel.loginResponse.collectAsState()
                var navigated by remember { mutableStateOf(false) }
                var hasWaited by remember { mutableStateOf(false) }
                val userUuid = remember { autoLoginViewModel.getuserUuid(context) }

                LaunchedEffect(userUuid) {
                    if (!userUuid.isNullOrBlank()) {
                        autoLoginViewModel.autoLogin(context)
                    }
                }

                LaunchedEffect(autoLoginResult) {
                    delay(1000)
                    if (navigated) return@LaunchedEffect
                    if (userUuid.isNullOrBlank()) {
                        hideSystemBars(false)
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                        navigated = true
                    } else if (autoLoginResult != null) {
                        loginResponse = autoLoginResult
                        hideSystemBars(false)
                        if (!autoLoginResult?.nickname.isNullOrEmpty()) {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("signup") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        navigated = true
                    } else {
                        delay(2000)
                        if (!navigated) {
                            hideSystemBars(false)
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                            navigated = true
                        }
                    }
                }

            }
            composable("onboarding") {
                OnboardingScreen(
                    onStartClick = {
                        navController.navigate("login") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            composable("login") {
                LoginScreen(onNextClick = { response, token ->
                    loginResponse = response
                    fcmToken = token
                    if (!response.nickname.isNullOrBlank()) {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("signup") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                })
            }
            composable("signup") {
                if (loginResponse != null) {
                    val prefs = context.getSharedPreferences("fcm", Context.MODE_PRIVATE)
                    val latestFcmToken = prefs.getString("fcm_token", null)
                    SignUpScreen(
                        loginResponse = loginResponse!!,
                        fcmToken = latestFcmToken,
                        nicknameViewModel = nicknameViewModel,
                        keywordViewModel = keywordViewModel,
                        categoryViewModel = categoryViewModel,
                        onFinish = { updatedLoginResponse ->
                            loginResponse = updatedLoginResponse
                            navController.navigate("main") {
                                popUpTo("signup") { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable("main") {
                MainScreen(
                    popupprogressViewModel = popupprogressViewModel,
                    popupcomingViewModel = popupcomingViewModel,
                    popupViewModel = popupViewModel,
                    recommendPopupViewModel = recommendPopupViewModel,
                    loginResponse = loginResponse,
                    fcmToken = fcmToken,
                    navController = navController,
                    userDataViewModel = userDataViewModel,
                    onUpdateLoginResponse = { updated ->
                        loginResponse = updated
                    },
                    deepLinkPopupUuid = deepLinkPopupUuid
                )
            }
        }
    }
}
