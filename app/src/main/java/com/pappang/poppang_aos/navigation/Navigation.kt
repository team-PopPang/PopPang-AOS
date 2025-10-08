package com.pappang.poppang_aos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.view.LoginScreen
import com.pappang.poppang_aos.view.OnboardingScreen
import com.pappang.poppang_aos.view.SignUpScreen
import com.pappang.poppang_aos.view.SplashScreen
import com.pappang.poppang_aos.view.HomeScreen
import com.pappang.poppang_aos.viewmodel.AddKeywordViewModel
import com.pappang.poppang_aos.viewmodel.CategoryItemViewModel
import com.pappang.poppang_aos.viewmodel.DuplicateNickname
import com.pappang.poppang_aos.viewmodel.AutoLoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.delay

@Composable
fun Navigation(
    nicknameViewModel: DuplicateNickname,
    keywordViewModel: AddKeywordViewModel,
    categoryViewModel: CategoryItemViewModel,
    hideSystemBars: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    var loginResponse by remember { mutableStateOf<LoginResponse?>(null) }
    var fcmToken by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val autoLoginViewModel: AutoLoginViewModel = viewModel()

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
                val uid = remember { autoLoginViewModel.getUid(context) }

                LaunchedEffect(uid) {
                    if (!uid.isNullOrBlank()) {
                        autoLoginViewModel.autoLogin(context)
                    }
                }

                LaunchedEffect(Unit) {
                    delay(1000)
                    hasWaited = true
                    if (navigated) return@LaunchedEffect
                    if (uid.isNullOrBlank()) {
                        hideSystemBars(false)
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                        navigated = true
                    } else if (autoLoginResult != null) {
                        val res = autoLoginResult
                        loginResponse = res
                        hideSystemBars(false)
                        if (!res?.nickname.isNullOrEmpty()) {
                            android.util.Log.d("Navigation", "route: home (ready within 1s)")
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            android.util.Log.d("Navigation", "route: signup (ready within 1s)")
                            navController.navigate("signup") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        navigated = true
                    }
                }

                LaunchedEffect(autoLoginResult, hasWaited) {
                    if (!hasWaited || navigated) return@LaunchedEffect
                    val res = autoLoginResult ?: return@LaunchedEffect
                    hideSystemBars(false)
                    android.util.Log.d("Navigation", "autoLoginResult.nickname: ${res.nickname}")
                    if (!res.nickname.isNullOrEmpty()) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("signup") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    navigated = true
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
                        navController.navigate("home") {
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
                    SignUpScreen(
                        loginResponse = loginResponse!!,
                        fcmToken = fcmToken,
                        nicknameViewModel = nicknameViewModel,
                        keywordViewModel = keywordViewModel,
                        categoryViewModel = categoryViewModel,
                        onFinish = {
                            navController.navigate("home") {
                                popUpTo("signup") { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable("home") {
                HomeScreen()
            }
        }
    }
}
