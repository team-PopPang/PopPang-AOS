package com.pappang.poppang_aos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.ui.theme.largeTitlie
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray2
import com.pappang.poppang_aos.ui.theme.title1

@Composable
fun KeywordScreen() {
    val keyword = remember { mutableStateOf(TextFieldValue("")) }
    Box(
        modifier = Modifier
            .background(Color(0xFFFFFFFF)),
    ){
        Column {
            Text(
                text = "키워드를\n입력해주세요.",
                style = largeTitlie,
                color = Color.Black,
                modifier = Modifier
                    .padding(start = 24.dp, top = 44.dp)
            )
            Text(
                text = "등록된 키워드에 맞춰 알림을 받아볼 수 있습니다.",
                style = title1,
                color = mainGray1,
                modifier = Modifier
                    .padding(start = 24.dp, top = 11.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Row {
                TextField(
                    value = keyword.value,
                    onValueChange = { keyword.value = it },
                    placeholder = { Text("ex) 화장품, 애니메이션", style = title1, color = mainGray2) },
                    modifier = Modifier
                        .weight(9f)
                        .padding(horizontal = 24.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        errorContainerColor = Color.White,
                        focusedIndicatorColor = mainGray1,
                        unfocusedIndicatorColor = mainGray1,
                        disabledIndicatorColor = mainGray1,
                        errorIndicatorColor = Color.Red,
                        cursorColor = mainGray1,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black,
                    )
                )
                CustomButton2(
                    onClick = {  },
                    text = "등록",
                    modifier = Modifier
                        .weight(3f)
                        .padding(start = 5.dp,end = 24.dp)
                )
            }
        }
    }
}

@Composable
@Preview
fun KeywordScreenPreview() {
    KeywordScreen()
}