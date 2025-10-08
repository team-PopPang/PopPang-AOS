package com.pappang.poppang_aos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.ui.theme.bottomBtn
import com.pappang.poppang_aos.ui.theme.mainOrange
import com.pappang.poppang_aos.ui.theme.title1

@Composable
fun CustomButton(onClick: () -> Unit, text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp) // 양쪽 24dp 패딩
            .background(mainOrange, shape = RoundedCornerShape(5.dp))
            .height(52.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            style = bottomBtn,
            color = Color.White,
            modifier = Modifier
                .align(Center)
        )
    }
}

@Composable
fun CustomButton2(onClick: () -> Unit, text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(mainOrange, shape = RoundedCornerShape(5.dp))
            .height(52.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            style = title1,
            color = Color.White,
            modifier = Modifier
                .align(Center)
        )
    }
}