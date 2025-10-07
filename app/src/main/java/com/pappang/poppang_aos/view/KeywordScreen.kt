package com.pappang.poppang_aos.view

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.ui.theme.largeTitlie
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray2
import com.pappang.poppang_aos.ui.theme.title1
import com.pappang.poppang_aos.viewmodel.AddKeywordViewModel

@Composable
fun KeywordScreen(keywordViewModel: AddKeywordViewModel) {
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
            Row(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
                verticalAlignment = CenterVertically
            ) {
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
                        focusedIndicatorColor = Color(0xFFF3F4F6),
                        unfocusedIndicatorColor = Color(0xFFF3F4F6),
                        disabledIndicatorColor = Color(0xFFF3F4F6),
                        errorIndicatorColor = Color.Red,
                        cursorColor = mainGray1,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black,
                    )
                )
                CustomButton2(
                    onClick = {
                        keywordViewModel.addKeyword(keyword.value.text)
                        keyword.value = TextFieldValue("")
                    },
                    text = "등록",
                    modifier = Modifier
                        .weight(3f)
                        .padding(start = 5.dp,end = 24.dp)
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            KeywordList(
                keywords = keywordViewModel.keywordList,
                onRemove = { keywordViewModel.removeKeyword(it) }
            )
        }
    }
}

@Composable
fun KeywordList(
    keywords: List<String>,
    onRemove: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        keywords.forEach { item ->
            Row(
                verticalAlignment = CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Text(
                    text = item,
                    style = title1,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "✕",
                    color = mainBlack,
                    modifier = Modifier
                        .clickable { onRemove(item) }
                        .padding(start = 8.dp)
                )
            }
        }
    }
}
