package com.poppang.PopPang.view

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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.poppang.PopPang.R
import com.poppang.PopPang.datastore.SearchQueryDataStore
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.ui.theme.Bold15
import com.poppang.PopPang.ui.theme.Medium12
import com.poppang.PopPang.ui.theme.Medium15
import com.poppang.PopPang.ui.theme.Medium18
import com.poppang.PopPang.ui.theme.Regular12
import com.poppang.PopPang.ui.theme.keyworldline
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray2
import com.poppang.PopPang.ui.theme.mainGray3
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainRed
import com.poppang.PopPang.viewmodel.AlarmKeywordViewModel
import com.poppang.PopPang.viewmodel.AlertViewModel
import com.poppang.PopPang.viewmodel.FavoriteViewModel
import com.poppang.PopPang.viewmodel.SearchViewModel
import com.poppang.PopPang.viewmodel.SearchViewModelFactory
import com.poppang.PopPang.viewmodel.ViewCountViewModel

@Composable
fun AlarmScreen(onClose: () -> Unit,
                SearchviewModel: SearchViewModel = viewModel(
                    factory = SearchViewModelFactory(
                        SearchQueryDataStore(LocalContext.current)
                    )
                ),
                AlarmKeywordviewModel: AlarmKeywordViewModel = viewModel(),
                alertViewModel: AlertViewModel = viewModel(),
                loginResponse: LoginResponse?,
                showDetail: Boolean = false,
                setShowDetail: (Boolean) -> Unit,
                favoriteViewModel: FavoriteViewModel
){
    var selectedIndex = remember { mutableStateOf(0) }
    var selectedPopup by remember { mutableStateOf<PopupEvent?>(null) }
    val userUuid = loginResponse?.userUuid ?: ""
    val alertpopupList by alertViewModel.alertPopupList.collectAsState()
    BackHandler { onClose() }
    LaunchedEffect(userUuid) {
        alertViewModel.fetchalertpopup(userUuid)
        AlarmKeywordviewModel.getKeywords(userUuid)
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
                0 -> ActivityTab(
                    alertViewModel = alertViewModel,
                    alertpopupList = alertpopupList,
                    refreshTrigger = showDetail, loginResponse = loginResponse,
                    favoriteViewModel = favoriteViewModel,
                    onShowDetail = { popup ->
                    selectedPopup = popup
                    setShowDetail(true)
                })
                1 -> keywordTab(loginResponse = loginResponse, searchviewModel = SearchviewModel, alarmkeywordviewModel = AlarmKeywordviewModel)
            }
        }
        if (showDetail && selectedPopup != null) {
            ContentDetail(
                popup = selectedPopup!!,
                onClose = { setShowDetail(false) },
                loginResponse = loginResponse,
                favoriteViewModel = favoriteViewModel
            )
        }
    }

}

@Composable
fun AlarmTopBar(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp)
    ) {
        Row(
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
            Text(
                text = "알림",
                style = Medium18,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 31.dp)
                    .align(CenterVertically),
                textAlign = Center
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
                    .height(37.dp)
                    .clickable { selectedIndex.value = 0 },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "활동",
                    style = Medium12,
                    color = if (selectedIndex.value == 0) mainRed else mainGray3,
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
                    .height(37.dp)
                    .clickable { selectedIndex.value = 1 },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "키워드 설정",
                    style = Medium12,
                    color = if (selectedIndex.value == 1) mainRed else mainGray3,
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
fun ActivityTab(
    alertViewModel: AlertViewModel,
    alertpopupList: List<PopupEvent>,
    favoriteViewModel: FavoriteViewModel,
    viewCountViewModel: ViewCountViewModel = viewModel(),
    refreshTrigger: Any? = null,
    loginResponse: LoginResponse? = null,
    onShowDetail: (PopupEvent) -> Unit
) {
    val userUuid = loginResponse?.userUuid.orEmpty()
    val favoritePopupUuids by favoriteViewModel.favoritePopupUuids.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 24.dp, end = 24.dp, bottom = 9.dp)
    ) {
        LazyColumn {
            items(alertpopupList) { popup ->
                val isLiked = favoritePopupUuids.contains(popup.popupUuid)
                var favoriteCount by remember { mutableStateOf(0) }
                var viewCount by remember { mutableStateOf(0) }
                LaunchedEffect(popup.popupUuid, refreshTrigger) {
                    favoriteViewModel.getFavoriteCount(popup.popupUuid) { count ->
                        favoriteCount = count.toInt()
                    }
                    viewCountViewModel.getTotalViewCount(popup.popupUuid) { count ->
                        viewCount = count.toInt()
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(163.dp)
                        .padding(vertical = 12.dp)
                        .clickable {
                            alertViewModel.updateAlertReadStatus(userUuid, popup.popupUuid)
                            onShowDetail(popup) }
                ) {
                    Row {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(popup.fullImageUrlList.getOrNull(0))
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build(),
                            contentDescription = popup.name,
                            modifier = Modifier
                                .height(133.dp)
                                .width(106.dp)
                                .align(CenterVertically),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = popup.roadAddress.split(" ").take(2).joinToString(" "),
                                        style = Regular12,
                                        color = mainBlack
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = popup.name,
                                        style = Bold15,
                                        color = mainBlack,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = popup.startDateFormatted + " - " + popup.endDateFormatted,
                                        style = Regular12,
                                        color = mainGray1
                                    )
                                }
                                Row(
                                    verticalAlignment = CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.eye_icon),
                                        contentDescription = "조회수 아이콘",
                                        tint = mainGray1,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = viewCount.toString(),
                                        style = Regular12,
                                        color = mainGray1,
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    IconButton(
                                        onClick = {
                                            val newLikeStatus = !isLiked
                                            if (newLikeStatus) {
                                                favoriteViewModel.addFavorite(userUuid, popup.popupUuid)
                                            } else {
                                                favoriteViewModel.deleteFavorite(userUuid, popup.popupUuid)
                                            }
                                            favoriteViewModel.getFavoriteCount(popup.popupUuid) { count ->
                                                favoriteCount = count.toInt()
                                            }
                                        },
                                        modifier = Modifier.size(12.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.heart_gray_icon),
                                            contentDescription = "Like Icon",
                                            tint = if (isLiked) Color.Red else Color.Unspecified
                                        )
                                    }
                                    Text(
                                        text = favoriteCount.toString(),
                                        style = Regular12,
                                        color = mainGray1,
                                    )
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
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

