package com.pappang.poppang_aos.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.ui.theme.google
import com.pappang.poppang_aos.ui.theme.homeTitle
import com.pappang.poppang_aos.ui.theme.kakao

@Composable
fun LoginScreen(onNextClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF)),
        contentAlignment = BottomCenter
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
        ) {
            Image(
                painter = painterResource(id = R.drawable.poppang_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .align(CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(50.dp))
            KakaoLoginButton(onClick = onNextClick, modifier = Modifier)
            Spacer(modifier = Modifier.height(10.dp))
            GoogleLoginButton(onClick = onNextClick, modifier = Modifier)
        }
    }
}

@Composable
fun GoogleLoginButton(onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(google, shape = RoundedCornerShape(5.dp))
            .border(width = 1.dp, color = Color(0xFFCCCCCC), shape = RoundedCornerShape(5.dp))
            .height(52.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Center)
        ) {
        Image(
            painter = painterResource(id = R.drawable.google_icon),
            contentDescription = null,
            modifier = Modifier
                .size(17.dp)
        )
        Text(
            text = "구글 로그인",
            style = homeTitle,
            color = Color(0xFF333333),
            modifier = Modifier
                .padding(start = 8.dp)
        )
        }
    }
}
@Composable
fun KakaoLoginButton(onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(kakao, shape = RoundedCornerShape(5.dp))
            .height(52.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.kakaotalk_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(17.dp)
            )
            Text(
                text = "카카오 로그인",
                style = homeTitle,
                color = Color(0xFF3D1D1C),
                modifier = Modifier
                    .padding(start = 8.dp)
            )
        }
    }
}

@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreen(onNextClick = {})
}