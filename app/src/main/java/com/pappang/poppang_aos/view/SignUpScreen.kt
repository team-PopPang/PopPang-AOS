package com.pappang.poppang_aos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.ui.theme.mainOrange

@Composable
fun SignUpScreen(
    currentPage: Int = 0,
    pageCount: Int = 3
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        Column {
            ProgressIndicator(
                currentPage = currentPage,
                pageCount = pageCount,
                modifier = Modifier
                    .padding(top = 44.dp)
            )
            NicknameScreen()
        }
        CustomButton(
            onClick = {},
            text = "다음",
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(
                        color = if (index <= currentPage) mainOrange else Color(0xFFEEEEEE))
            )
        }
    }
}

@Composable
@Preview
fun SignUpScreenPreview() {
    SignUpScreen()
}