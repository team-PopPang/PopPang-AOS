package com.pappang.poppang_aos.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.ui.theme.largeTitlie
import com.pappang.poppang_aos.ui.theme.mainWhite

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFC300))
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_icon),
            contentDescription = null,
            modifier = Modifier
                .align(Center)
                .size(110.dp) // 원하는 크기로 dp 값 조정
        )
        Text(
            text = "POP PANG",
            style = largeTitlie,
            color = White,
            modifier = Modifier
                .align(Center)
                .offset(y = 60.dp) // 이미지 아래로 내림 (필요에 따라 dp 조정)
        )
    }
}

@Preview
@Composable
fun SplashScreenPreview(){
    SplashScreen()
}