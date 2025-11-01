package com.pappang.poppang_aos.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.model.PopupEvent
import com.pappang.poppang_aos.ui.theme.Black20
import com.pappang.poppang_aos.ui.theme.Bold10
import com.pappang.poppang_aos.ui.theme.Bold15
import com.pappang.poppang_aos.ui.theme.Bold17
import com.pappang.poppang_aos.ui.theme.Light10
import com.pappang.poppang_aos.ui.theme.Medium11
import com.pappang.poppang_aos.ui.theme.Medium12
import com.pappang.poppang_aos.ui.theme.Medium13
import com.pappang.poppang_aos.ui.theme.Medium17
import com.pappang.poppang_aos.ui.theme.Regular11
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.Regular15
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray2
import com.pappang.poppang_aos.ui.theme.mainGray4
import com.pappang.poppang_aos.ui.theme.mainGray5
import com.pappang.poppang_aos.ui.theme.mainOrange
import java.time.LocalDate.now
import java.time.LocalDate.parse
import java.time.format.DateTimeFormatter.ofPattern
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    hideSatausBar: (Boolean) -> Unit = {},
    showDetail: Boolean = false,
    setShowDetail: (Boolean) -> Unit,
    showSearch: Boolean = false,
    setSearchScreen: (Boolean) -> Unit,
    showAlarm: Boolean = false,
    setShowAlarm: (Boolean) -> Unit,
    popupprogressList: List<PopupEvent>,
    popupcomingList: List<PopupEvent>,
    loginResponse: LoginResponse?
) {
    var selectedPopup by remember { mutableStateOf<PopupEvent?>(null) }
    if (showSearch) {
        SearchScreen(onClose = { setSearchScreen(false) }, loginResponse = loginResponse, hideSatausBar = hideSatausBar)
    }
    else if(showAlarm) {
        AlarmScreen(onClose = { setShowAlarm(false) },loginResponse = loginResponse)
    }
    else {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopSearchBar(
                    modifier = Modifier,
                    onSearchBarClick = { setSearchScreen(true) },
                    onAlarmClick = { setShowAlarm(true) }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(15.dp))
                    BannerCarousel()
                    Spacer(modifier = Modifier.height(50.dp))
                    BoxCarousel(popupcomingList = popupcomingList, onShowDetail = { popup ->
                        selectedPopup = popup
                        setShowDetail(true)
                    })
                    Spacer(modifier = Modifier.height(50.dp))
                    FilterSection()
                    Spacer(modifier = Modifier.height(15.dp))
                    MainContent(popupprogressList = popupprogressList, onShowDetail = { popup ->
                        selectedPopup = popup
                        setShowDetail(true)
                    })
                }
            }
        }
        if (showDetail && selectedPopup != null) {
            ContentDetail(
                popup = selectedPopup!!,
                onClose = { setShowDetail(false) },
                hideSatausBar = hideSatausBar
            )
        }
    }
}

@Composable
private fun TopSearchBar(modifier: Modifier, onSearchBarClick: () -> Unit = {}, onAlarmClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 15.dp,end =15.dp, top = 15.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "POP PANG",
                style = Black20,
                color = mainOrange,
                modifier = Modifier
                    .weight(1f)
            )
            IconButton(onClick = { onSearchBarClick() }) {
                Icon(
                    painter = painterResource(id = R.drawable.serch_icon),
                    contentDescription = "search",
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(20.dp),
                    tint = Color.Unspecified
                )
            }
            IconButton(
                onClick = { onAlarmClick() },
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
    }
}

@Composable
fun BannerCarousel(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(initialPage = 0) { 5 }
    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(194.dp),
        contentPadding = PaddingValues(end = 15.dp),
        pageSpacing = 15.dp,
        modifier = modifier
            .padding(start = 15.dp)
    ) { page ->
        Box(
            modifier = Modifier
                .width(194.dp)
                .height(271.dp)
                .clickable { Log.d("BannerCarousel", "Banner $page clicked") }
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg),
                contentDescription = "Banner $page",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = linearGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0x00333333),
                                0.6f to Color(0x10333333),
                                1.0f to Color(0x38333333)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 271f)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 11.dp, bottom = 11.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "팝마트 <라부부의 수상한 실험실> 팝업스토어",
                        style = Bold15,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.location_outlined),
                            contentDescription = "Location Icon",
                            modifier = Modifier.size(10.dp),
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "서울 성동구",
                            style = Medium12,
                            color = mainGray5,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BoxCarousel(onShowDetail: (PopupEvent) -> Unit, popupcomingList: List<PopupEvent>) {
    val sortedList = popupcomingList.sortedBy { it.startDate }
    val pagerState = rememberPagerState(initialPage = 0) { sortedList.size }
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp)
        ) {
            Column {
                Text(
                    text = "COMING SOON",
                    style = Medium11,
                    color = mainOrange,
                    modifier = Modifier
                )
                Text(
                    text = "곧 생기는 팝업",
                    style = Bold15,
                    color = mainBlack,
                    modifier = Modifier
                        .padding(top = 5.dp)
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.arrow_up),
                contentDescription = "Arrow Right",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 15.dp)
                    .size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(283.dp),
            contentPadding = PaddingValues(start = 0.dp, end = 15.dp),
            pageSpacing = 15.dp,
            modifier = Modifier
                .padding(start = 15.dp),
        ) { page ->
            val popup = sortedList[page]
            Box(
                modifier = Modifier
                    .width(283.dp)
                    .height(138.dp)
                    .clickable { onShowDetail(popup) }
                    .border(1.dp, mainGray5, RoundedCornerShape(5.dp))
                    .padding(10.dp)
            ) {
                Box(modifier = Modifier.width(94.4.dp).height(118.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(popup.fullImageUrlList.getOrNull(0))
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                        contentDescription = popup.name,
                        modifier = Modifier,
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 104.4.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "오픈 D-" + runCatching {
                                val formatter = ofPattern("yyyy-MM-dd")
                                val start = parse(popup.startDate, formatter)
                                val today = now()
                                val days = ChronoUnit.DAYS.between(today, start)
                                days.coerceAtLeast(0).toString()
                            }.getOrDefault(""),
                            style = Bold10,
                            color = mainOrange,
                            modifier = Modifier
                                .padding(top = 5.dp)
                        )
                        Text(
                            text = popup.name,
                            style = Medium13,
                            color = mainBlack,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(top = 6.dp)
                        )
                    }
                    Text(
                        text = popup.roadAddress.split(" ").take(2).joinToString(" "),
                        style = Regular11,
                        color = mainGray1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun LocalFilterButton() {
    var showSheet by remember { mutableStateOf(false) }
    var selectedRegion by remember { mutableStateOf("전체") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .clickable { showSheet = true }
    ) {
        Row {
            Text(
                text = selectedRegion,
                color = mainBlack,
                style = Medium17
            )
            Icon(
                painter = painterResource(R.drawable.arrow_up),
                contentDescription = "지역 선택",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(16.dp)
                    .rotate(if (showSheet) 270f else 90f)
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 30.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "지역",
                        style = Bold17,
                        color = mainBlack
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.close_icon),
                        contentDescription = "닫기",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showSheet = false },
                        tint = mainBlack
                    )
                }
                val regionItems = listOf(
                    "전체" to "전체",
                    "서울" to "강남/역삼/삼성/논현",
                    "부산" to "서초/신사/방배",
                    "대구" to "잠실",
                    "강원" to "구로/영등포/여의도",
                    "충청도" to "신림/사당/금천/동작"
                )

                regionItems.forEachIndexed { index, (region, subAreas) ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { selectedRegion = region; showSheet = false }.background(Color.White), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(65.dp).height(46.dp).background(if (region == selectedRegion) Color.White else mainGray4), contentAlignment = Alignment.Center) {
                            Text(text = region, style = Medium12, color = if (region == selectedRegion) mainOrange else mainGray1)
                        }
                        Box(modifier = Modifier.width(1.dp).height(46.dp).background(mainGray5))
                        Box(modifier = Modifier.weight(1f).background(Color.White).padding(vertical = 8.dp, horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                            Text(text = subAreas, style = Medium12, color = mainBlack)
                        }
                    }
                    if (index != regionItems.lastIndex) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.width(65.dp).height(1.dp).background(if (region == selectedRegion) Color.White else mainGray4))
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(mainGray5))
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(bottom = 20.dp))
            }
        }
    }
}

@Composable
fun SortType() {
    var showSheet by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("최신순") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier
        .clickable { showSheet = true }
        .background(Color.White, RoundedCornerShape(20.dp))
        .border(0.dp, mainGray1, RoundedCornerShape(20.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp)
    ){
        Row {
            Text(
                text = selectedSort,
                color = mainGray1,
                style = Light10
            )
            Icon(
                painter = painterResource(R.drawable.arrow_up),
                contentDescription = "정렬 선택",
                modifier = Modifier
                    .padding(start = 3.dp)
                    .size(10.dp)
                    .rotate(if (showSheet) 270f else 90f),
                tint = mainGray2
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }, sheetState = sheetState, dragHandle = null, containerColor = Color.White) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth().
                        padding(start = 12.dp, top = 28.dp, bottom = 30.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "정렬",
                        style = Bold17,
                        color = mainBlack,
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.close_icon),
                        contentDescription = "닫기",
                        modifier = Modifier.size(24.dp)
                            .clickable { showSheet = false },
                        tint = mainBlack
                    )
                }
                val sortItems = listOf("최신순", "인기순", "마감임박순")
                sortItems.forEachIndexed { index, sortType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSort = sortType; showSheet = false }
                            .background(Color.White),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(if (sortType == selectedSort) R.drawable.button_on else R.drawable.button_off),
                                    contentDescription = null,
                                    tint = Color.Unspecified
                                )
                                Text(
                                    text = sortType,
                                    style = Regular15,
                                    color = mainBlack,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                )
            }
        }
    }
}

@Composable
fun FilterSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LocalFilterButton()
        SortType()
    }
}

@Composable
fun MainContent(onShowDetail: (PopupEvent) -> Unit, popupprogressList: List<PopupEvent>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp)
    ) {
        popupprogressList.chunked(2).forEach { pair ->
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
                            .clickable { onShowDetail(popup) }
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
                                text = popup.startDateFormatted + " - " + popup.endDateFormatted,
                                style = Regular12.copy(letterSpacing = (-1).sp),
                                color = mainGray1,
                                modifier = Modifier
                                    .padding(top = 5.dp)
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
fun HomeScreenPreview() {
    HomeScreen(setSearchScreen = {}, setShowDetail = {}, setShowAlarm = {}, popupprogressList = listOf(), popupcomingList = listOf(), loginResponse = LoginResponse("", "", "", "", "", "", "",false) )
}

