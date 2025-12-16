package com.poppang.PopPang.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.poppang.PopPang.ui.theme.ExtraBold24
import com.poppang.PopPang.ui.theme.Medium15
import com.poppang.PopPang.ui.theme.keyworldline
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray2
import com.poppang.PopPang.viewmodel.AddKeywordViewModel

@Composable
fun KeywordScreen(keywordViewModel: AddKeywordViewModel) {
    val keyword = remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .background(Color(0xFFFFFFFF)),
    ){
        Column {
            Text(
                text = "키워드를\n입력해주세요.",
                style = ExtraBold24,
                color = mainBlack,
                modifier = Modifier
                    .padding(start = 24.dp, top = 44.dp)
            )
            Text(
                text = "등록된 키워드에 맞춰 알림을 받아볼 수 있습니다.",
                style = Medium15,
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
                    onValueChange = {
                        val filtered = it.text.replace("\n", "")
                        keyword.value = it.copy(text = filtered, selection = TextRange(filtered.length))
                    },
                    placeholder = { Text("ex) 화장품, 애니메이션", style = Medium15, color = mainGray2) },
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
                        if (keywordViewModel.keywordList.size >= 5) {
                            Toast.makeText(context, "키워드는 최대 5개까지 저장할 수 있습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            keywordViewModel.addKeyword(keyword.value.text)
                            keyword.value = TextFieldValue("")
                        }
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
                    .padding(top = 15.dp, bottom = 10.dp)
            ) {
                Text(
                    text = item,
                    style = Medium15,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "✕",
                    color = mainBlack,
                    modifier = Modifier
                        .clickable { onRemove(item) }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(keyworldline)
            )
        }
    }
}

@Composable
@Preview
fun KeywordListPreview() {
    val sampleKeywords = listOf("화장품", "애니메이션", "게임", "음악")
    KeywordList(
        keywords = sampleKeywords,
        onRemove = {}
    )
}