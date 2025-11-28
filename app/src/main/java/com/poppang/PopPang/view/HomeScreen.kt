package com.poppang.PopPang.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.poppang.PopPang.R
import com.poppang.PopPang.model.LoginResponse
import com.poppang.PopPang.model.PopupEvent
import com.poppang.PopPang.ui.theme.Black20
import com.poppang.PopPang.ui.theme.Bold10
import com.poppang.PopPang.ui.theme.Bold15
import com.poppang.PopPang.ui.theme.Bold17
import com.poppang.PopPang.ui.theme.Light10
import com.poppang.PopPang.ui.theme.Medium11
import com.poppang.PopPang.ui.theme.Medium12
import com.poppang.PopPang.ui.theme.Medium13
import com.poppang.PopPang.ui.theme.Medium17
import com.poppang.PopPang.ui.theme.Regular11
import com.poppang.PopPang.ui.theme.Regular12
import com.poppang.PopPang.ui.theme.Regular15
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray2
import com.poppang.PopPang.ui.theme.mainGray3
import com.poppang.PopPang.ui.theme.mainGray4
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.viewmodel.FavoriteViewModel
import com.poppang.PopPang.viewmodel.HomePopupfilterViewModel
import com.poppang.PopPang.viewmodel.RegionsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate.now
import java.time.LocalDate.parse
import java.time.format.DateTimeFormatter.ofPattern
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    showDetail: Boolean = false,
    setShowDetail: (Boolean) -> Unit,
    showSearch: Boolean = false,
    setSearchScreen: (Boolean) -> Unit,
    showAlarm: Boolean = false,
    setShowAlarm: (Boolean) -> Unit,
    popupprogressList: List<PopupEvent>,
    popupcomingList: List<PopupEvent>,
    recommendpopupList: List<PopupEvent>,
    popupList: List<PopupEvent>,
    loginResponse: LoginResponse?,
    favoriteViewModel: FavoriteViewModel,
    regionsViewModel: RegionsViewModel,
    homePopupfilterViewModel: HomePopupfilterViewModel = viewModel(),
    deepLinkPopupUuid: String? = null
) {
    var selectedRegion by remember { mutableStateOf("전체") }
    var selectedDistrict by remember { mutableStateOf("전체") }
    var selectedSort by remember { mutableStateOf("NEWEST") }
    val homepopupfilterList by homePopupfilterViewModel.homePopupfilterList.collectAsState()
    var selectedPopup by remember { mutableStateOf<PopupEvent?>(null) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var showScrollToTop by remember { mutableStateOf(false) }


    LaunchedEffect(selectedRegion, selectedDistrict, selectedSort, loginResponse?.userUuid) {
        homePopupfilterViewModel.fetchhomepopupfilter(
            userUuid = loginResponse?.userUuid.orEmpty(),
            region = selectedRegion,
            district = selectedDistrict,
            homeSortStandard = selectedSort
        )
    }

    LaunchedEffect(scrollState.value) {
        showScrollToTop = scrollState.value > 300 // 300dp 정도 내려가면 표시
    }

    LaunchedEffect(popupList) {
        if (deepLinkPopupUuid.isNullOrBlank()) return@LaunchedEffect
        if (selectedPopup != null) return@LaunchedEffect  // 이미 열려 있으면 무시

        Log.d("DeepLink", "Searching for popupUuid: $deepLinkPopupUuid")
        val target = popupList.firstOrNull { it.popupUuid == deepLinkPopupUuid }
        if (target != null) {
            selectedPopup = target
            setShowDetail(true)
            Log.d("DeepLink", "Found and showing popup: ${target.name}")
        }
    }

    if (showSearch) {
        SearchScreen(
            onClose = { setSearchScreen(false) },
            loginResponse = loginResponse,
            favoriteViewModel = favoriteViewModel,
            showDetail = showDetail,
            setShowDetail = setShowDetail,
        )
    } else if (showAlarm) {
        AlarmScreen(
            onClose = { setShowAlarm(false) },
            loginResponse = loginResponse,
            showDetail = showDetail,
            setShowDetail = setShowDetail,
            favoriteViewModel = favoriteViewModel
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
            ) {
                TopSearchBar(
                    modifier = Modifier,
                    onSearchBarClick = { setSearchScreen(true) },
                    onAlarmClick = { setShowAlarm(true) }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Row() {
                        loginResponse?.nickname?.let {
                            Text(
                                text= it,
                                style = Bold17,
                                color = mainOrange,
                                modifier = Modifier
                                    .padding(start = 15.dp)
                            )
                            Text(
                                text= "님을 위한 팝업",
                                style = Bold17,
                                color = mainBlack,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    BannerCarousel(recommendpopupList = recommendpopupList, onShowDetail = { popup ->
                        selectedPopup = popup
                        setShowDetail(true)
                    })
                    Spacer(modifier = Modifier.height(50.dp))
                    BoxCarousel(popupcomingList = popupcomingList, onShowDetail = { popup ->
                        selectedPopup = popup
                        setShowDetail(true)
                    })
                    Spacer(modifier = Modifier.height(50.dp))
                    FilterSection(
                        regionsViewModel = regionsViewModel,
                        selectedRegion = selectedRegion,
                        selectedDistrict = selectedDistrict,
                        selectedSort = selectedSort,
                        onRegionChange = { region, district ->
                            selectedRegion = region
                            selectedDistrict = district
                        },
                        onSortChange = { sort ->
                            selectedSort = sort
                        }
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    MainContent(
                        homepopupfilterList = homepopupfilterList, onShowDetail = { popup ->
                            selectedPopup = popup
                            setShowDetail(true)
                        },
                        loginResponse = loginResponse,
                        favoriteViewModel = favoriteViewModel
                    )
                }
            }
            if (showScrollToTop) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    },
                    containerColor = Color.White,
                    shape =CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_icon),
                        contentDescription = "맨 위로",
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(90f),
                        tint = Color.Unspecified
                    )
                }
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
private fun TopSearchBar(modifier: Modifier, onSearchBarClick: () -> Unit = {}, onAlarmClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 15.dp, end = 15.dp, top = 15.dp)
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
fun BannerCarousel(modifier: Modifier = Modifier, recommendpopupList: List<PopupEvent>, onShowDetail: (PopupEvent) -> Unit) {
    val sortedList = recommendpopupList.sortedBy { it.startDate }
    val pagerState = rememberPagerState(initialPage = 0) { sortedList.size}
        HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(194.dp),
        contentPadding = PaddingValues(end = 15.dp),
        pageSpacing = 15.dp,
        modifier = modifier
            .padding(start = 15.dp)
    ) { page ->
            val popup = sortedList[page]
        Box(
            modifier = Modifier
                .width(194.dp)
                .height(271.dp)
                .clickable { onShowDetail(popup) }
        ) {
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
                        text = popup.name,
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
                            text = popup.roadAddress.split(" ").take(2).joinToString(" "),
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
                Box(modifier = Modifier
                    .width(94.4.dp)
                    .height(118.dp)) {
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
fun LocalFilterButton(selectedRegion: String, onRegionSelected: (String) -> Unit, regionsViewModel: RegionsViewModel) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentRegion by remember { mutableStateOf(selectedRegion) }
    val regionsList by regionsViewModel.regions.collectAsState()
    val baseMap: Map<String, List<String>> = if (regionsList.isNotEmpty()) {
        regionsList.associate { it.region to it.districtList }
    } else {
        emptyMap()
    }
    val regionMap: Map<String, List<String>> = if (baseMap.isEmpty()) {
        mapOf("전체" to listOf("전체"))
    } else {
        val ordered = linkedMapOf<String, List<String>>()
        ordered["전체"] = listOf("전체")
        baseMap["서울"]?.let { ordered["서울"] = it }
        for ((k, v) in baseMap) {
            if (k != "서울") ordered.putIfAbsent(k, v)
        }
        ordered
    }
    val regionItems = regionMap.keys.toList()
    val subAreaList = regionMap[currentRegion] ?: listOf("전체")
    val cellHeight = 46.dp
    val tableHeight = (regionItems.size * 46).dp

    Box(
        modifier = Modifier
            .width(60.dp)
            .clickable { showSheet = true }
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(0.dp, mainGray1, RoundedCornerShape(20.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row {
            Text(
                text = "지역",
                color = mainGray1,
                style = Light10
            )
            Icon(
                painter = painterResource(R.drawable.arrow_up),
                contentDescription = "지역 선택",
                modifier = Modifier
                    .padding(start = 3.dp)
                    .size(10.dp)
                    .rotate(if (showSheet) 270f else 90f),
                tint = mainGray2
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
                modifier = Modifier.padding(horizontal = 28.dp)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(mainGray3)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tableHeight)
                        .background(Color.White)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .width(65.dp)
                            .background(mainGray4)
                            .height(tableHeight)
                    ) {
                        itemsIndexed(regionItems) { idx, region ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cellHeight)
                                    .background(if (currentRegion == region) Color.White else mainGray4)
                                    .clickable { currentRegion = region },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = region,
                                    style = Medium12,
                                    color = if (currentRegion == region) mainOrange else mainGray1
                                )
                            }
                            if (idx != regionItems.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(mainGray4)
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(mainGray5)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White)
                            .height(tableHeight)
                    ) {
                        itemsIndexed(subAreaList) { idx, subArea ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cellHeight)
                                    .clickable {
                                        onRegionSelected(
                                            if (subArea == "전체") currentRegion else "$currentRegion $subArea"
                                        )
                                        showSheet = false
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = subArea,
                                    style = Medium12,
                                    color = mainBlack
                                )
                            }
                            if (idx != subAreaList.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(mainGray5)
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(mainGray3)
                )
                Spacer(modifier = Modifier.padding(bottom = 20.dp))
            }
        }
    }
}

@Composable
fun SortType(selectedSort: String, onSortSelected: (String) -> Unit) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sortItems = listOf(
        "최신순" to "NEWEST",
        "조회순" to "MOST_VIEWED",
        "마감임박순" to "CLOSING_SOON"
    )
    Box(modifier = Modifier
        .width(80.dp)
        .clickable { showSheet = true }
        .background(Color.White, RoundedCornerShape(20.dp))
        .border(0.dp, mainGray1, RoundedCornerShape(20.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ){
        Row {
            Text(
                text = sortItems.find { it.second == selectedSort }?.first ?: "최신순",
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
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 28.dp, bottom = 30.dp, end = 12.dp),
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
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showSheet = false },
                        tint = mainBlack
                    )
                }
                sortItems.forEach { (kor,eng) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSortSelected(eng)
                                showSheet = false
                            }
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
                                    painter = painterResource(if (selectedSort == eng) R.drawable.button_on else R.drawable.button_off),
                                    contentDescription = null,
                                    tint = Color.Unspecified
                                )
                                Text(
                                    text = kor,
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
fun FilterSection(
    regionsViewModel: RegionsViewModel,
    selectedRegion: String,
    selectedDistrict: String,
    selectedSort: String,
    onRegionChange: (String, String) -> Unit,
    onSortChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedRegion + if (selectedDistrict != "전체") " $selectedDistrict" else "",
            style = Medium17,
            color = mainBlack
        )
        Box{
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                LocalFilterButton(
                    selectedRegion = selectedRegion,
                    onRegionSelected = { regionWithDistrict ->
                        val parts = regionWithDistrict.split(" ")
                        val region = parts.getOrNull(0) ?: "전체"
                        val district = parts.getOrNull(1) ?: "전체"
                        onRegionChange(region, district)
                    },
                    regionsViewModel = regionsViewModel
                )
                SortType(
                    selectedSort = selectedSort,
                    onSortSelected = { onSortChange(it) }
                )
            }
        }
    }
}

@Composable
fun MainContent(
    onShowDetail: (PopupEvent) -> Unit,
    homepopupfilterList: List<PopupEvent>,
    loginResponse: LoginResponse?,
    favoriteViewModel: FavoriteViewModel
) {
    val userUuid = loginResponse?.userUuid.orEmpty()
    val favoritePopupUuids by favoriteViewModel.favoritePopupUuids.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp)
    ) {
        homepopupfilterList.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                pair.forEach { popup ->
                    val isLiked = favoritePopupUuids.contains(popup.popupUuid)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onShowDetail(popup) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(217.5.dp),
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(popup.fullImageUrlList.getOrNull(0))
                                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = popup.name,
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        val newLikeStatus = !isLiked
                                        if (newLikeStatus) {
                                            favoriteViewModel.addFavorite(userUuid, popup.popupUuid)
                                        } else {
                                            favoriteViewModel.deleteFavorite(
                                                userUuid,
                                                popup.popupUuid
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 5.dp, end = 14.dp)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (isLiked) R.drawable.heart_gray_icon else R.drawable.heart_icon),
                                        contentDescription = "Like Icon",
                                        modifier = Modifier,
                                        tint = if (isLiked) Color.Red else Color.Unspecified
                                    )
                                }
                            }
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