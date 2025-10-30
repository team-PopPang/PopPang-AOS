package com.pappang.poppang_aos.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.datastore.SearchQueryDataStore
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.ui.theme.Bold15
import com.pappang.poppang_aos.ui.theme.Medium12
import com.pappang.poppang_aos.ui.theme.Medium15
import com.pappang.poppang_aos.ui.theme.Medium18
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.keyworldline
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray2
import com.pappang.poppang_aos.ui.theme.mainGray3
import com.pappang.poppang_aos.ui.theme.mainGray5
import com.pappang.poppang_aos.ui.theme.mainRed
import com.pappang.poppang_aos.viewmodel.AlarmKeywordViewModel
import com.pappang.poppang_aos.viewmodel.SearchViewModel
import com.pappang.poppang_aos.viewmodel.SearchViewModelFactory

@Composable
fun AlarmScreen(onClose: () -> Unit,
                SearchviewModel: SearchViewModel = viewModel(
                    factory = SearchViewModelFactory(
                        SearchQueryDataStore(LocalContext.current)
                    )
                ),
                AlarmKeywordviewModel: AlarmKeywordViewModel = viewModel(),
                loginResponse: LoginResponse?,) {
    var selectedIndex = remember { mutableStateOf(0) }
    BackHandler { onClose() }
    LaunchedEffect(loginResponse?.userUuid) {
        AlarmKeywordviewModel.getKeywords(loginResponse?.userUuid ?: "")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column {
            AlarmTopBar(onClose = onClose)
            AlarmItem(selectedIndex = selectedIndex)
            when (selectedIndex.value) {
                0 -> ActivityTab()
                1 -> keywordTab(loginResponse = loginResponse, searchviewModel = SearchviewModel, alarmkeywordviewModel = AlarmKeywordviewModel)
            }
        }
    }
}

@Composable
fun AlarmTopBar(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_icon),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .padding(start = 24.dp)
                    .size(14.dp)
                    .clickable { onClose() }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "알림",
                style = Medium18,
                color = Color.Black,
                modifier = Modifier
                    .align(CenterVertically)
                    .clickable{}
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.setting_icon),
                contentDescription = "알림 설정",
                modifier = Modifier
                    .padding(end = 24.dp)
                    .size(16.dp)
            )
        }
    }
}

@Composable
fun AlarmItem( selectedIndex: MutableState<Int>) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(37.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "활동",
                    style = Medium12,
                    color = if (selectedIndex.value == 0) mainRed else mainGray3,
                    modifier = Modifier
                        .clickable { selectedIndex.value = 0 }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(if (selectedIndex.value == 0) mainRed else mainGray5)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(37.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "키워드 설정",
                    style = Medium12,
                    color = if (selectedIndex.value == 1) mainRed else mainGray3,
                    modifier = Modifier
                        .clickable { selectedIndex.value = 1 }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(if (selectedIndex.value == 1) mainRed else mainGray5),
                )
            }
        }
    }
}

@Composable
fun ActivityTab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 24.dp, end = 24.dp, bottom = 9.dp)
    ) {
        LazyColumn {
            items(10) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(163.dp)
                        .padding(vertical = 12.dp)
                ) {
                    Row {
                        Image(
                            painter = painterResource(id = R.drawable.bg),
                            contentDescription = "알림 아이콘",
                            modifier = Modifier
                                .height(133.dp)
                                .width(106.dp)
                                .align(CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalArrangement = SpaceBetween
                            ) {
                                Box {
                                    Column {
                                        Text(
                                            text = "부산 동구",
                                            style = Regular12,
                                            color = mainBlack
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "새로운 팝핑이 도착했어요!",
                                            style = Bold15,
                                            color = mainBlack,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "25.05.12 - 25.06.12",
                                            style = Regular12,
                                            color = mainGray1
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Row(
                                        verticalAlignment = CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.eye_icon),
                                            contentDescription = "시간 아이콘",
                                            tint = mainGray1,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = " 100",
                                            style = Regular12,
                                            color = mainGray1,
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.heart_icon),
                                            contentDescription = "새알림 아이콘",
                                            tint = mainRed,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = " 50",
                                            style = Regular12,
                                            color = mainGray1,
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(0.5.dp)
                        .fillMaxWidth()
                        .background(mainGray5)
                )
            }

        }
    }
}

@Composable
fun keywordTab(loginResponse: LoginResponse?, searchviewModel: SearchViewModel, alarmkeywordviewModel: AlarmKeywordViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column {
            HomeKeywordScreen(
                loginResponse = loginResponse,
                viewModel = alarmkeywordviewModel,
            )
            Spacer(modifier = Modifier.height(30.dp))
            RecentSearchContent(
                recentQueries = searchviewModel.recentQueries.value,
                loginResponse = loginResponse,
                viewModel = searchviewModel,
            )
        }
    }
}

@Composable
fun HomeKeywordScreen(loginResponse: LoginResponse?, viewModel: AlarmKeywordViewModel) {
    val keyword = remember { mutableStateOf(TextFieldValue("")) }
    val keywords by viewModel.keywords.collectAsState()
    Box(
        modifier = Modifier
            .background(Color(0xFFFFFFFF)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
                verticalAlignment = CenterVertically
            ) {
                TextField(
                    value = keyword.value,
                    onValueChange = { keyword.value = it },
                    placeholder = { Text("알림받을 키워드를 입력해주세요.", style = Medium12, color = mainGray2) },
                    modifier = Modifier
                        .weight(1f)
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
                IconButton(
                    onClick = {
                        if (keyword.value.text.isNotEmpty()) {
                            viewModel.insertKeyword(
                                userUuid = loginResponse?.userUuid ?: "",
                                newAlertKeyword = keyword.value.text
                            )
                            keyword.value = TextFieldValue("")
                        }
                    },
                    modifier = Modifier

                ){
                    Icon(
                        painter = painterResource(id = R.drawable.plus_icon),
                        contentDescription = "등록 버튼",
                        tint = Color.Unspecified
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            HomeKeywordList(
                keywords = keywords,
                onRemove = { item ->
                    viewModel.deleteKeyword(
                        userUuid = loginResponse?.userUuid ?: "",
                        deleteAlertKeyword = item
                    )
                }
            )
        }
    }
}

@Composable
fun HomeKeywordList(
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

