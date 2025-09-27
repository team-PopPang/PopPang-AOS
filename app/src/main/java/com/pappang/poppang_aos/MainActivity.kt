package com.pappang.poppang_aos

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsets.Type.navigationBars
import android.view.WindowInsets.Type.statusBars
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import com.pappang.poppang_aos.ui.theme.PopPangAOSTheme
import com.pappang.poppang_aos.view.OnboardingScreen
import com.pappang.poppang_aos.view.SplashScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PopPangAOSTheme {
                var showSplash by remember { mutableStateOf(true) }
                val controller = window.insetsController

                if (showSplash) {
                    setDecorFitsSystemWindows(window, false)
                    controller?.hide(
                        statusBars() or
                                navigationBars()
                    )
                    controller?.systemBarsBehavior =
                        BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    setDecorFitsSystemWindows(window, true)
                    controller?.show(
                        statusBars() or
                                navigationBars()
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { _ ->
                    if (showSplash) {
                        SplashScreen()
                        LaunchedEffect(Unit) {
                            delay(1000)
                            showSplash = false
                        }
                    } else {
                        OnboardingScreen()
                    }
                }
            }
        }
    }
}