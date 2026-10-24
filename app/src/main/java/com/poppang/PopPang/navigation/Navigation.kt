package com.poppang.PopPang.navigation

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.poppang.PopPang.datastore.AuthTokenStore
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.SignupStatus
import com.poppang.PopPang.network.SessionStatus
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
import com.poppang.PopPang.viewmodel.SessionRestoreState
import com.poppang.PopPang.viewmodel.UserDataViewModel

// Saved UI state contains profile fields only; tokens stay in the encrypted session store.
private val LoginResponseSaver = Saver<LoginResponse?, List<Any?>>(
    save = { user -> user?.let {
        listOf(it.userUuid, it.provider, it.email, it.nickname, it.role, it.isAlerted, it.signupStatus.name)
    } },
    restore = { saved -> LoginResponse(
        userUuid = saved[0] as String?,
        provider = saved[1] as String?,
        email = saved[2] as String?,
        nickname = saved[3] as String?,
        role = saved[4] as String?,
        isAlerted = saved[5] as Boolean?,
        signupStatus = SignupStatus.valueOf(saved[6] as String),
    ) },
)

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
    deepLinkPopupUuid: String? = null,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val session = AuthTokenStore.session
    val sessionState by session.state.collectAsState()
    var loginResponse by rememberSaveable(stateSaver = LoginResponseSaver) { mutableStateOf<LoginResponse?>(null) }
    val latestUserData by userDataViewModel.userData.collectAsState()
    val appPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val fcmToken by com.poppang.PopPang.datastore.FcmTokenStore.token.collectAsState()

    LaunchedEffect(latestUserData, sessionState) {
        if (sessionState.status == SessionStatus.SIGNED_IN && latestUserData?.userUuid == session.userUuid()) {
            latestUserData?.let { loginResponse = it }
        }
    }
    LaunchedEffect(sessionState) {
        if (sessionState.status == SessionStatus.SIGNED_OUT) {
            loginResponse = null
            userDataViewModel.reset()
            popupprogressViewModel.reset()
            popupcomingViewModel.reset()
            popupViewModel.reset()
            recommendPopupViewModel.reset()
            nicknameViewModel.onNicknameChange("")
            keywordViewModel.keywordList.clear()
            categoryViewModel.selectedCategories.clear()
            if (navController.currentDestination?.route in listOf("main", "signup")) {
                navController.navigate("login") { popUpTo(0) { inclusive = true } }
            }
        }
    }

    Scaffold { innerPadding ->
        NavHost(navController, startDestination = "splash", modifier = Modifier.padding(innerPadding)) {
            composable("splash") {
                val restoreState by autoLoginViewModel.state.collectAsState()
                LaunchedEffect(Unit) { autoLoginViewModel.restoreSession() }
                LaunchedEffect(restoreState) {
                    val target = when (val restored = restoreState) {
                        SessionRestoreState.Loading, is SessionRestoreState.Failed -> null
                        SessionRestoreState.SignedOut -> if (appPreferences.getBoolean("onboarding_seen", false)) "login" else "onboarding"
                        is SessionRestoreState.Restored -> {
                            loginResponse = restored.user
                            if (restored.user.signupStatus == SignupStatus.PENDING) "signup" else "main"
                        }
                    }
                    if (target != null) {
                        hideSystemBars(false)
                        navController.navigate(target) { popUpTo("splash") { inclusive = true } }
                    }
                }
                val failure = restoreState as? SessionRestoreState.Failed
                if (failure != null) {
                    Column {
                        Text(failure.message)
                        Button(onClick = { autoLoginViewModel.restoreSession() }) { Text("다시 시도") }
                        Button(onClick = { autoLoginViewModel.signOut() }) { Text("로그인 화면으로") }
                    }
                } else {
                    SplashScreen()
                }
            }
            composable("onboarding") {
                OnboardingScreen(onStartClick = {
                    appPreferences.edit().putBoolean("onboarding_seen", true).apply()
                    navController.navigate("login") { popUpTo("onboarding") { inclusive = true } }
                })
            }
            composable("login") {
                LoginScreen(onNextClick = { response, _ ->
                    loginResponse = response
                    val target = if (response.signupStatus == SignupStatus.PENDING) "signup" else "main"
                    navController.navigate(target) { popUpTo("login") { inclusive = true } }
                })
            }
            composable("signup") {
                BackHandler { session.cancelSignup() }
                val user = loginResponse
                if (user?.signupStatus == SignupStatus.PENDING) {
                    SignUpScreen(
                        loginResponse = user,
                        fcmToken = fcmToken,
                        nicknameViewModel = nicknameViewModel,
                        keywordViewModel = keywordViewModel,
                        categoryViewModel = categoryViewModel,
                        onFinish = { profile ->
                            loginResponse = profile
                            navController.navigate("main") { popUpTo("signup") { inclusive = true } }
                        },
                    )
                } else if (sessionState.status != SessionStatus.SIGNED_OUT) {
                    LaunchedEffect(Unit) { navController.navigate("splash") { popUpTo(0) { inclusive = true } } }
                }
            }
            composable("main") {
                val user = loginResponse
                if (user != null && sessionState.status == SessionStatus.SIGNED_IN) {
                    MainScreen(
                        popupprogressViewModel = popupprogressViewModel,
                        popupcomingViewModel = popupcomingViewModel,
                        popupViewModel = popupViewModel,
                        recommendPopupViewModel = recommendPopupViewModel,
                        loginResponse = user,
                        fcmToken = fcmToken,
                        navController = navController,
                        userDataViewModel = userDataViewModel,
                        onUpdateLoginResponse = { loginResponse = it },
                        deepLinkPopupUuid = deepLinkPopupUuid,
                    )
                } else if (sessionState.status != SessionStatus.SIGNED_OUT) {
                    LaunchedEffect(Unit) { navController.navigate("splash") { popUpTo(0) { inclusive = true } } }
                }
            }
        }
    }
}
