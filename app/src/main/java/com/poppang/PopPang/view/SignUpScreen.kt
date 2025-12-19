package com.poppang.PopPang.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.viewmodel.AddKeywordViewModel
import com.poppang.PopPang.viewmodel.CategoryItemViewModel
import com.poppang.PopPang.viewmodel.DuplicateNickname
import com.poppang.PopPang.viewmodel.SignUpViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SignUpScreen(
    pageCount: Int = 2,
    loginResponse: LoginResponse,
    fcmToken: String?,
    onFinish: (LoginResponse) -> Unit = {},
    nicknameViewModel: DuplicateNickname,
    keywordViewModel: AddKeywordViewModel,
    categoryViewModel: CategoryItemViewModel
) {
    val currentPage = remember { mutableStateOf(0) }
    val context = LocalContext.current
    val signUpViewModel: SignUpViewModel = viewModel()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        Column {
            ProgressIndicator(
                currentPage = currentPage.value,
                pageCount = pageCount,
                modifier = Modifier
                    .padding(top = 44.dp)
            )
            AnimatedContent(
                targetState = currentPage.value,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(350)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(350)
                    )
                }
            ) { page ->
                when (page) {
                    0 -> NicknameScreen(viewModel = nicknameViewModel)
                    1 -> KeywordScreen(
                        keywordViewModel = keywordViewModel,
                        onSkip = {
                            signUpViewModel.signUpUser(
                                loginResponse = loginResponse,
                                fcmToken = fcmToken,
                                nicknameViewModel = nicknameViewModel,
                                keywordViewModel = keywordViewModel,
                                categoryViewModel = categoryViewModel
                            ) { success ->
                                if (success) {
                                    val updatedLoginResponse = loginResponse.copy(
                                        nickname = nicknameViewModel.nickname,
                                        fcmToken = fcmToken
                                    )
                                    onFinish(updatedLoginResponse)
                                }
                            }
                        }
                    )
                }
            }
        }
        CustomButton(
            onClick = {
                if (currentPage.value == 0) {
                    if (nicknameViewModel.isSuccess && currentPage.value < pageCount - 1) {
                        currentPage.value += 1
                    }
                } else {
                    signUpViewModel.signUpUser(
                        loginResponse = loginResponse,
                        fcmToken = fcmToken,
                        nicknameViewModel = nicknameViewModel,
                        keywordViewModel = keywordViewModel,
                        categoryViewModel = categoryViewModel
                    ) { success ->
                        if (success) {
                            val updatedLoginResponse = loginResponse.copy(
                                nickname = nicknameViewModel.nickname,
                                fcmToken = fcmToken
                            )
                            onFinish(updatedLoginResponse)
                        }
                    }
                }
                      },
            text = if (currentPage.value < pageCount - 1) "다음" else "시작하기",
            modifier = Modifier
                .align(BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 20.dp
                ),
            backgroundColor = when (currentPage.value) {
                0 -> if (nicknameViewModel.isSuccess) mainOrange else mainGray5
                else -> mainOrange
            }
        )
    }
}

@Composable
fun ProgressIndicator(currentPage: Int, pageCount: Int, modifier: Modifier) {
    var progress by remember { mutableStateOf(0f) }
    val targetProgress = (currentPage + 1).toFloat() / pageCount

    LaunchedEffect(targetProgress) {
        progress = targetProgress
    }

    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400)
    ).value

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(Color(0xFFEEEEEE))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(3.dp)
                .background(mainOrange)
        )
    }
}
