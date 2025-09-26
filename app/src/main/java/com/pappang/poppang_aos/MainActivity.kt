package com.pappang.poppang_aos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.pappang.poppang_aos.ui.theme.PopPangAOSTheme
import com.pappang.poppang_aos.view.SplashScreen // SplashScreen import 추가

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PopPangAOSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    SplashScreen()
                }
            }
        }
    }
}
