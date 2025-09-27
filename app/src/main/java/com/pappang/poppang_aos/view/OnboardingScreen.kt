package com.pappang.poppang_aos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.ui.theme.bottomBtn
import com.pappang.poppang_aos.ui.theme.largeTitlie


@Composable
fun OnboardingScreen(
    onStartClick: () -> Unit = {}
) {
    val titles = listOf("제목 1", "제목 2", "제목 3", "제목 4")
    val descriptions = listOf(
        "설명 1입니다.",
        "설명 2입니다.",
        "설명 3입니다.",
        "설명 4입니다."
    )
    val buttonLabels = listOf("다음", "다음", "다음", "시작하기  →")

    var page by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF)),
    ) {
        Column(
            modifier = Modifier
                .align(Center)
                .fillMaxWidth(),
            horizontalAlignment = CenterHorizontally
        ) {
            Text(
                text = titles[page],
                style = largeTitlie,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = descriptions[page],
                style = bottomBtn ,
                color = Color.Black
            )
        }
        CustomButton(
            onClick = {
                if (page < 3) {
                    page++
                } else {
                    onStartClick()
                }
            },
            text = buttonLabels[page],
            modifier = Modifier
                .align(BottomCenter)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 20.dp)
        )
    }
}




@Composable
@Preview
fun OnboardingScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFC300))
    )
    OnboardingScreen()
}