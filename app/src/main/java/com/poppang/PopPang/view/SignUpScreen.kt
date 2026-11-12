package com.poppang.PopPang.view

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.view.renewal.RenewalSignupFlow
import com.poppang.PopPang.viewmodel.AddKeywordViewModel
import com.poppang.PopPang.viewmodel.CategoryItemViewModel
import com.poppang.PopPang.viewmodel.DuplicateNickname
import com.poppang.PopPang.viewmodel.PopularKeywordViewModel
import com.poppang.PopPang.viewmodel.SignUpViewModel
import com.poppang.PopPang.viewmodel.SignupNotificationViewModel

@Composable
fun SignUpScreen(
    loginResponse: LoginResponse,
    fcmToken: String?,
    onFinish: (LoginResponse) -> Unit = {},
    nicknameViewModel: DuplicateNickname,
    keywordViewModel: AddKeywordViewModel,
    categoryViewModel: CategoryItemViewModel,
    onBack: () -> Unit = {},
) {
    val signup: SignUpViewModel = viewModel()
    val popularKeywords: PopularKeywordViewModel = viewModel()
    val notifications: SignupNotificationViewModel = viewModel()
    val finishWithoutOptIn: () -> Unit = { signup.completedProfile?.let(onFinish) }
    val notificationOptIn =
        rememberNotificationOptIn(
            onAllowed = { signup.completedProfile?.let { notifications.enable(it, onFinish) } },
            onLater = finishWithoutOptIn,
        )
    notifications.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {
                notifications.clearError()
                finishWithoutOptIn()
            },
            title = { Text("알림 설정을 켜지 못했어요") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    notifications.clearError()
                    notificationOptIn.request()
                }) { Text("다시 시도") }
            },
            dismissButton = {
                TextButton(onClick = {
                    notifications.clearError()
                    finishWithoutOptIn()
                }) { Text("나중에") }
            },
        )
    }
    LaunchedEffect(Unit) { categoryViewModel.fetchCategoryItems(forSignup = true) }
    LaunchedEffect(nicknameViewModel) { nicknameViewModel.recommendInitialSignupNickname() }
    RenewalSignupFlow(
        nickname = signup.completedProfile?.nickname ?: nicknameViewModel.nickname,
        onNicknameChange = nicknameViewModel::onNicknameChange,
        onExit = onBack,
        nicknameMessage = nicknameViewModel.checkResult,
        nicknameValid = nicknameViewModel.isSuccess,
        nicknameError = nicknameViewModel.isError,
        onCheckNickname = { nicknameViewModel.checkNickname(forSignup = true) },
        onRecommendNickname = { nicknameViewModel.recommendNickname(forSignup = true) },
        nicknameRecommending = nicknameViewModel.isRecommending,
        nicknameChecking = nicknameViewModel.isChecking,
        nicknameRecommendationError = nicknameViewModel.recommendationError,
        categories = categoryViewModel.categories.map { it.recommendName },
        selectedCategories = categoryViewModel.selectedCategories.map { it.recommendName },
        onToggleCategory = { name ->
            categoryViewModel.categories.firstOrNull { it.recommendName == name }?.let(categoryViewModel::toggleCategory)
        },
        keywords = keywordViewModel.keywordList,
        onAddKeyword = keywordViewModel::addKeyword,
        onRemoveKeyword = keywordViewModel::removeKeyword,
        popularKeywords = popularKeywords.keywords,
        popularKeywordsLoading = popularKeywords.isLoading,
        popularKeywordsError = popularKeywords.errorMessage,
        onKeywordStepEntered = { popularKeywords.load(forSignup = true) },
        onRetryPopularKeywords = { popularKeywords.load(forSignup = true, force = true) },
        // The existing server contract stays at five until the renewal API is agreed.
        keywordLimit = 5,
        categoryLoading = categoryViewModel.isLoading,
        categoryError = categoryViewModel.errorMessage,
        onRetryCategories = categoryViewModel::retryFetchCategoryItems,
        submitting = signup.isSubmitting,
        submitError = signup.errorMessage,
        completed = signup.completedProfile != null,
        starting = notifications.isEnabling || notificationOptIn.waiting,
        onCompletedBack = finishWithoutOptIn,
        onSubmit = { skip ->
            signup.signUpUser(loginResponse, fcmToken, nicknameViewModel, keywordViewModel, categoryViewModel, skipKeywords = skip) {}
        },
        onStart = notificationOptIn.request,
    )
}
