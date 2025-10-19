package com.pappang.poppang_aos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.ui.theme.Medium12
import com.pappang.poppang_aos.ui.theme.Medium15
import com.pappang.poppang_aos.ui.theme.mainOrange

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
            style = Medium15,
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
            style = Medium12,
            color = Color.White,
            modifier = Modifier
                .align(Center)
        )
    }
}

@Composable
fun CustomButton3(onClick: () -> Unit, text: String,  modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(5.dp))
            .height(40.dp)
            .clickable { onClick() }
            .border(width = 1.dp, color = mainOrange, shape = RoundedCornerShape(5.dp))
    ) {
        Text(
            text = text,
            style = Medium12,
            color = mainOrange,
            modifier = Modifier
                .align(Center)
        )
    }
}
@Composable
fun CustomButton4(onClick: () -> Unit, text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(mainOrange, shape = RoundedCornerShape(5.dp))
            .height(40.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            style = Medium12,
            color = Color.White,
            modifier = Modifier
                .align(Center)
        )
    }
}


@Composable
@Preview
fun CustomButtonPreview() {
    CustomButton3(onClick = {}, text = "커스텀 버튼", modifier = Modifier)
}
@Composable
@Preview
fun CustomButton2Preview() {
    CustomButton4(onClick = {}, text = "커스텀 버튼2", modifier = Modifier)
}