package com.pappang.poppang_aos

import android.os.Bundle
import android.view.WindowInsets.Type.navigationBars
import android.view.WindowInsets.Type.statusBars
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import com.kakao.sdk.common.KakaoSdk
import com.pappang.poppang_aos.navigation.Navigation
import com.pappang.poppang_aos.ui.theme.PopPangAOSTheme
import com.pappang.poppang_aos.viewmodel.AddKeywordViewModel
import com.pappang.poppang_aos.viewmodel.CategoryItemViewModel
import com.pappang.poppang_aos.viewmodel.DuplicateNickname
import com.pappang.poppang_aos.viewmodel.PopupComingViewModel
import com.pappang.poppang_aos.viewmodel.PopupViewModel
import com.pappang.poppang_aos.viewmodel.SearchViewModel

class MainActivity : ComponentActivity() {
    private val nicknameViewModel: DuplicateNickname by viewModels()
    private val keywordViewModel: AddKeywordViewModel by viewModels()
    private val categoryViewModel: CategoryItemViewModel by viewModels()
    private val popupViewModel: PopupViewModel by viewModels()
    private val popupcomingViewModel: PopupComingViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KakaoSdk.init(this, BuildConfig.KAKAO_KEY)
        enableEdgeToEdge()
        setContent {
            PopPangAOSTheme {
                val controller = window.insetsController
                var hideSystemBars by remember { mutableStateOf(true) }
                var hidestatusBar by remember { mutableStateOf(false) }
                LaunchedEffect(hideSystemBars) {
                    if (hideSystemBars) {
                        setDecorFitsSystemWindows(window, false)
                        controller?.hide(statusBars() or navigationBars())
                        controller?.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        setDecorFitsSystemWindows(window, true)
                        controller?.show(statusBars() or navigationBars())
                    }
                }
                LaunchedEffect(hidestatusBar) {
                    if (hidestatusBar) {
                        setDecorFitsSystemWindows(window, false)
                        controller?.hide(statusBars())
                        controller?.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        setDecorFitsSystemWindows(window, true)
                        controller?.show(statusBars())
                    }
                }
                Navigation(
                    nicknameViewModel = nicknameViewModel,
                    keywordViewModel = keywordViewModel,
                    categoryViewModel = categoryViewModel,
                    popupViewModel = popupViewModel,
                    popupcomingViewModel = popupcomingViewModel,
                    searchViewModel = searchViewModel,
                    hideSystemBars = { hide -> hideSystemBars = hide },
                    hideStatusBar = { hide -> hidestatusBar = hide }
                )
            }
        }
    }
}