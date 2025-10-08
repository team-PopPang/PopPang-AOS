package com.pappang.poppang_aos.view

import android.util.Log
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.ui.theme.mainOrange
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.pappang.poppang_aos.viewmodel.DuplicateNickname
import com.pappang.poppang_aos.viewmodel.AddKeywordViewModel
import com.pappang.poppang_aos.viewmodel.CategoryItemViewModel
import com.pappang.poppang_aos.model.UserSignUpRequest
import com.pappang.poppang_aos.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.Toast
import com.pappang.poppang_aos.model.LoginResponse
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pappang.poppang_aos.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(
    pageCount: Int = 3,
    loginResponse: LoginResponse,
    fcmToken: String?,
    onFinish: () -> Unit = {},
    nicknameViewModel: DuplicateNickname,
    keywordViewModel: AddKeywordViewModel,
    categoryViewModel: CategoryItemViewModel
) {
    val currentPage = remember { mutableStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current
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
            when (currentPage.value) {
                0 -> NicknameScreen(viewModel = nicknameViewModel)
                1 -> KeywordScreen(keywordViewModel = keywordViewModel)
                2 -> CategorySelectionScreen(categoryViewModel = categoryViewModel)
            }
        }
        CustomButton(
            onClick = {
                if (currentPage.value == 0) {
                    if (nicknameViewModel.isSuccess && currentPage.value < pageCount - 1) {
                        currentPage.value += 1
                    }
                } else {
                    if (currentPage.value < pageCount - 1) {
                        currentPage.value += 1
                    } else {
                        signUpViewModel.signUpUser(
                            loginResponse = loginResponse,
                            fcmToken = fcmToken,
                            nicknameViewModel = nicknameViewModel,
                            keywordViewModel = keywordViewModel,
                            categoryViewModel = categoryViewModel
                        ) { success ->
                            if (success) {
                                onFinish()
                            } else {
                                Toast.makeText(context, "회원가입 실패", Toast.LENGTH_SHORT).show()
                            }
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
                )
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
