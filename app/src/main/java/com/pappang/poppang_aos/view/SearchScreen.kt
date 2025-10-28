package com.pappang.poppang_aos.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.model.PopupEvent
import com.pappang.poppang_aos.ui.theme.Bold15
import com.pappang.poppang_aos.ui.theme.Medium12
import com.pappang.poppang_aos.ui.theme.Regular11
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray4
import com.pappang.poppang_aos.ui.theme.mainOrange
import com.pappang.poppang_aos.viewmodel.SearchViewModel

@Composable
fun SearchScreen(onClose: () -> Unit, viewModel: SearchViewModel = viewModel()) {
    val query = remember { mutableStateOf("") }
    val isSearched = remember { mutableStateOf(false) }
    val popupList = viewModel.popupList.value
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    BackHandler { onClose() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                TextField(
                    value = query.value,
                    onValueChange = {
                        query.value = it
                        isSearched.value = false
                    },
                    placeholder = {
                        Text(
                            text = "궁금한 장소를 검색해 보세요.",
                            style = Regular11,
                            color = mainGray1
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .heightIn(min = 45.dp)
                        .weight(1f)
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(3.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = mainGray4,
                        unfocusedContainerColor = mainGray4,
                        disabledContainerColor = mainGray4,
                        errorContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFF3F4F6),
                        unfocusedIndicatorColor = Color(0xFFF3F4F6),
                        disabledIndicatorColor = Color(0xFFF3F4F6),
                        errorIndicatorColor = Color.Red,
                        cursorColor = mainGray1,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black,
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            viewModel.search(query.value)
                            viewModel.addRecentQuery(query.value)
                            isSearched.value = true
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.serch_icon),
                                contentDescription = "search",
                                modifier = Modifier
                                    .padding(end = 20.dp)
                                    .size(20.dp),
                                tint = Color.Unspecified
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.search(query.value)
                            viewModel.addRecentQuery(query.value)
                            isSearched.value = true
                        }
                    )
                )
                IconButton(
                    onClick = { /* 알림 */ },
                    modifier = Modifier

                ) {
                    Icon(
                        painter = painterResource(R.drawable.bell_icon),
                        contentDescription = "bell",
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .size(23.dp),
                        tint = Color.Unspecified
                    )
                }
            }
            if (!isSearched.value) {
                RecentSearchContent(recentQueries = viewModel.recentQueries.value)
            } else {
                SearchContent(popupList = popupList)
            }
        }
    }
}

@Composable
fun RecentSearchContent(recentQueries: List<String>) {
    Spacer(modifier = Modifier.height(20.dp))
    Column(modifier = Modifier.padding(start = 24.dp)) {
        Row {
            Text(
                text = "이준태",
                style = Medium12,
                color = mainOrange
            )
            Text(
                text = "님의 최근 본 검색어예요.",
                style = Medium12,
                color = mainBlack
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
        recentQueries.forEach { query ->
            CustomButton5(
                onClick = { /* TODO: 해당 검색어로 검색 */ },
                text = query,
                modifier = Modifier
            )}
        }
    }
}

@Composable
fun SearchContent(popupList: List<PopupEvent>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp)
    ) {
        items(popupList.chunked(2)) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                pair.forEach { popup ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { }
                    ) {
                        Column {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(popup.fullImageUrlList.getOrNull(0))
                                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = popup.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(217.5.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = popup.roadAddress.split(" ").take(2).joinToString(" "),
                                style = Regular12,
                                color = mainBlack,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                            )
                            Text(
                                text = popup.name,
                                style = Bold15,
                                color = mainBlack,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(top = 5.dp)
                            )
                            Text(
                                text = popup.startDate + " - " + popup.endDate,
                                style = Regular12,
                                color = mainGray1,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
                if (pair.size == 1)
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )
            }
        }
    }
}

@Composable
@Preview
fun SearchScreenPreview() {
    SearchScreen(onClose = {})
}