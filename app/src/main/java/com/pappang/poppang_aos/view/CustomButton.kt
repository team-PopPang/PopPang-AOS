package com.pappang.poppang_aos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.ui.theme.Medium12
import com.pappang.poppang_aos.ui.theme.Medium15
import com.pappang.poppang_aos.ui.theme.Regular9
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray5
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
fun CustomButton5(onClick: () -> Unit, text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(20.dp))
            .height(30.dp)
            .border(width = 1.dp, color = mainGray5, shape = RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = modifier
                .align(Center),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = text,
                style = Regular9,
                color = mainBlack,
                modifier = modifier
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
            Text(
                text = "✕",
                style = Regular9,
                color = mainBlack,
                modifier = modifier
                    .clickable { onClick()}
                    .padding(end = 10.dp, top = 8.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
@Preview
fun CustomButton5Preview() {
    CustomButton5(onClick = {}, text = "취소", modifier = Modifier)
}