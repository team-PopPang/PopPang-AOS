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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.ui.theme.Bold10
import com.pappang.poppang_aos.ui.theme.Bold15
import com.pappang.poppang_aos.ui.theme.Bold17
import com.pappang.poppang_aos.ui.theme.Light10
import com.pappang.poppang_aos.ui.theme.Medium12
import com.pappang.poppang_aos.ui.theme.Medium13
import com.pappang.poppang_aos.ui.theme.Medium17
import com.pappang.poppang_aos.ui.theme.Regular11
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.Regular15
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray14
import com.pappang.poppang_aos.ui.theme.mainGray2
import com.pappang.poppang_aos.ui.theme.mainGray3
import com.pappang.poppang_aos.ui.theme.mainGray4
import com.pappang.poppang_aos.ui.theme.mainGray5
import com.pappang.poppang_aos.ui.theme.mainOrange

@Composable
fun HomeScreen(hideSatausBar: (Boolean) -> Unit = {}, showDetail: Boolean = false, setShowDetail: (Boolean) -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                TopSearchBar(
                    modifier = Modifier
                        .width(0.dp)
                        .weight(1f)
                )
                IconButton(
                    onClick = { /* 알림 */ },
                    modifier = Modifier
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.bell_icon),
                        contentDescription = "bell",
                        modifier = Modifier.size(23.dp),
                        tint = Color.Unspecified
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                BannerCarousel()
                Spacer(modifier = Modifier.height(40.dp))
                BoxCarousel()
                Spacer(modifier = Modifier.height(40.dp))
                FilterSection()
                Spacer(modifier = Modifier.height(15.dp))
                MainContent(onShowDetail = { setShowDetail(true) })
            }
        }
    }
    if (showDetail) {
        ContentDetail(onClose = { setShowDetail(false) },hideSatausBar)
    }
}

@Composable
private fun TopSearchBar(modifier: Modifier) {
    var query by remember { mutableStateOf("") }
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = modifier.heightIn(min = 45.dp),
        singleLine = true,
        maxLines = 1,
        placeholder = { Text(text = "궁금한 장소를 검색해 보세요.", style = Regular12, color = mainGray1) },
        trailingIcon = {
            IconButton(onClick = { /* 검색 */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.serch_icon),
                    contentDescription = "search",
                    modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = mainGray4,
            unfocusedContainerColor = mainGray4,
            cursorColor = mainBlack,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun BannerCarousel(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(initialPage = 0) { 5 }
    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(194.dp),
        contentPadding = PaddingValues(end = 64.dp),
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
fun BoxCarousel(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(initialPage = 0) { 5 }
    Column {
        Box(
            modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp)
        ) {
            Column {
                Text(
                    text = "COMING SOON",
                    style = Medium12,
                    color = mainOrange,
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                )
                Text(
                    text = "곧 생기는 팝업",
                    style = Bold17,
                    color = mainBlack,
                    modifier = Modifier
                        .padding(top = 6.dp,
                            bottom = 10.dp)
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

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(283.dp),
            contentPadding = PaddingValues(end = 64.dp),
            pageSpacing = 15.dp,
            modifier = modifier
                .padding(start = 15.dp)
        ) { page ->
            Box(
                modifier = Modifier
                    .width(283.dp)
                    .height(138.dp)
                    .clickable { Log.d("BannerCarousel", "Banner $page clicked") }
                    .border(1.dp, mainGray14, RoundedCornerShape(5.dp))
                    .padding(10.dp)
            ) {
                Box(modifier = Modifier.width(94.4.dp).height(118.dp)){
                    Image(painter = painterResource(id = R.drawable.bg), contentDescription = "Box $page", contentScale = ContentScale.Crop)
                }
                Column(
                    modifier = Modifier
                        .padding(start = 104.4.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "오픈 D-10",
                            style = Bold10,
                            color = mainOrange,
                            modifier = Modifier
                                .padding(top = 5.dp)
                        )
                        Text(
                            text = "팝업스토어 <라부부의 수상한 실험실> 팝업스토어",
                            style = Medium13,
                            color = mainBlack,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(top = 6.dp)
                        )
                    }
                    Text(
                        text = "서울 성동구", style = Regular11,
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
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 30.dp),
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

    Box(modifier = Modifier.clickable { showSheet = true }.background(mainGray5, RoundedCornerShape(20.dp)).border(1.dp, mainGray3, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 8.dp)) {
        Row {
            Text(text = selectedSort, color = mainGray1, style = Light10)
            Icon(painter = painterResource(R.drawable.arrow_up), contentDescription = "정렬 선택", modifier = Modifier.padding(start = 3.dp).size(10.dp).rotate(if (showSheet) 270f else 90f), tint = mainGray2)
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
                        padding(top = 20.dp, bottom = 30.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "정렬",
                        style = Bold17,
                        color = mainBlack
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
fun MainContent(onShowDetail: () ->Unit) {
    val images = List(10) { R.drawable.bg }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        images.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                pair.forEach { resId ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onShowDetail() }
                    ) {
                        Column {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "이미지",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(217.5.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = "서울 성동구",
                                style = Regular12,
                                color = mainBlack,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                            )
                            Text(
                                text = "팝업스토어 <라부부의 수상한 실험실> 팝업스토어",
                                style = Bold15,
                                color = mainBlack,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "2024.08.01 ~ 2024.08.31",
                                style = Regular12,
                                color = mainGray1
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
    HomeScreen()
}

