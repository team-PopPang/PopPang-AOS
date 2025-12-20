package com.poppang.PopPang.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
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
import com.poppang.PopPang.ui.theme.Bold12
import com.poppang.PopPang.ui.theme.Bold15
import com.poppang.PopPang.ui.theme.Bold17
import com.poppang.PopPang.ui.theme.Light10
import com.poppang.PopPang.ui.theme.Medium12
import com.poppang.PopPang.ui.theme.Medium17
import com.poppang.PopPang.ui.theme.Medium18
import com.poppang.PopPang.ui.theme.Medium8
import com.poppang.PopPang.ui.theme.Regular12
import com.poppang.PopPang.ui.theme.Regular15
import com.poppang.PopPang.ui.theme.mainAmber
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
import com.poppang.PopPang.viewmodel.SelectPopupViewModel
import com.poppang.PopPang.viewmodel.ViewCountViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale.KOREAN


@Composable
fun CalendarScreen(
    popupList: List<PopupEvent>,
    showDetail: Boolean = false,
    setShowDetail: (Boolean) -> Unit,
    showAlarm: Boolean = false,
    setShowAlarm: (Boolean) -> Unit,
    loginResponse: LoginResponse?,
    favoriteViewModel: FavoriteViewModel,
    viewCountViewModel: ViewCountViewModel = viewModel(),
    homePopupfilterViewModel: HomePopupfilterViewModel = viewModel(),
    selectPopupViewModel: SelectPopupViewModel,
    regionsViewModel: RegionsViewModel
) {
    val selectedDate = remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showScrollToTop by remember { mutableStateOf(false) }
    var selectedRegion by remember { mutableStateOf("전체") }
    var selectedDistrict by remember { mutableStateOf("전체") }
    var selectedSort by remember { mutableStateOf("NEWEST") }

    val homepopupfilterList by homePopupfilterViewModel.homePopupfilterList.collectAsState()
    val favoritePopupUuids by favoriteViewModel.favoritePopupUuids.collectAsState()
    val userUuid = loginResponse?.userUuid.orEmpty()

    var selectedPopup by remember { mutableStateOf<PopupEvent?>(null) }
    val selectPopupList by selectPopupViewModel.selectpopupList.collectAsState()
    var detailPopup by remember { mutableStateOf<PopupEvent?>(null) }

    // 날짜 + 필터 기준으로 필터링된 리스트
    val filteredList = remember(selectedDate.value, homepopupfilterList) {
        selectedDate.value?.let { date ->
            homepopupfilterList.filter { popup ->
                val start = LocalDate.parse(popup.startDate)
                val end = LocalDate.parse(popup.endDate)
                !date.isBefore(start) && !date.isAfter(end)
            }
        } ?: emptyList()
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        showScrollToTop =
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 300
    }

    LaunchedEffect(selectedRegion, selectedDistrict, selectedSort, loginResponse?.userUuid) {
        homePopupfilterViewModel.fetchhomepopupfilter(
            userUuid = loginResponse?.userUuid.orEmpty(),
            region = selectedRegion,
            district = selectedDistrict,
            homeSortStandard = selectedSort
        )
    }

    LaunchedEffect(showDetail, selectedPopup) {
        if (showDetail && selectedPopup != null) {
            selectPopupViewModel.SelectPopupEvents(
                userUuid = loginResponse?.userUuid.orEmpty(),
                popupUuid = selectedPopup!!.popupUuid
            )
        }
    }

    LaunchedEffect(selectPopupList, showDetail, selectedPopup) {
        if (showDetail && selectedPopup != null) {
            detailPopup = selectPopupList.firstOrNull { it.popupUuid == selectedPopup!!.popupUuid }
        }
    }

    if (showAlarm) {
        AlarmScreen(
            onClose = { setShowAlarm(false) },
            loginResponse = loginResponse,
            showDetail = showDetail,
            setShowDetail = setShowDetail,
            favoriteViewModel = favoriteViewModel,
            selectPopupViewModel = selectPopupViewModel,
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                CalendarTopBar(onAlarmClick = { setShowAlarm(true) })

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {

                    item {
                        CustomCalendar(
                            popupList = homepopupfilterList,
                            selectedDate = selectedDate.value,
                            onDateSelected = { selectedDate.value = it }
                        )
                    }

                    item {
                        if (selectedDate.value != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top =20.dp, bottom = 10.dp, start = 24.dp, end = 24.dp),
                                horizontalArrangement = SpaceBetween,
                                verticalAlignment = CenterVertically
                            ) {
                                Row {
                                    Text(
                                        text = "${selectedDate.value!!.dayOfMonth}일",
                                        style = Medium12,
                                        color = mainBlack,
                                    )
                                    Text(
                                        text = selectedDate.value!!.dayOfWeek.getDisplayName(
                                            TextStyle.SHORT,
                                            KOREAN
                                        ) + "요일",
                                        style = Medium12,
                                        color = mainBlack,
                                        modifier = Modifier.padding(start = 5.dp)
                                    )
                                }
                                Row {
                                    CalendarLocalFilterButton(
                                        selectedRegion = selectedRegion,
                                        onRegionSelected = { regionWithDistrict ->
                                            val parts = regionWithDistrict.split(" ")
                                            val region = parts.getOrNull(0) ?: "전체"
                                            val district = parts.getOrNull(1) ?: "전체"
                                            selectedRegion = region
                                            selectedDistrict = district
                                        },
                                        regionsViewModel = regionsViewModel
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    CalendarSortType(
                                        selectedSort = selectedSort,
                                        onSortSelected = { selectedSort = it }
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(
                        items = filteredList,
                        key = { _, popup -> popup.popupUuid }
                    ) { _, popup ->

                        val isLiked = favoritePopupUuids.contains(popup.popupUuid)
                        var favoriteCount by remember { mutableStateOf(0) }
                        var viewCount by remember { mutableStateOf(0) }

                        LaunchedEffect(popup.popupUuid, showDetail) {
                            favoriteViewModel.getFavoriteCount(userUuid, popup.popupUuid) { count ->
                                favoriteCount = count.toInt()
                            }
                            viewCountViewModel.getTotalViewCount(userUuid, popup.popupUuid) { count ->
                                viewCount = count.toInt()
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(163.dp)
                                    .padding(vertical = 12.dp)
                                    .clickable {
                                        selectedPopup = popup
                                        setShowDetail(true) }
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
                                            modifier = Modifier
                                                .fillMaxHeight(),
                                            verticalArrangement = SpaceBetween
                                        ) {
                                            Box {
                                                Column {
                                                    Text(
                                                        text = popup.roadAddress.split(" ").take(2)
                                                            .joinToString(" "),
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
                                                        style = Regular12.copy(letterSpacing = (-1).sp),
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
                                                        contentDescription = "조회수 아이콘",
                                                        tint = mainGray1,
                                                        modifier = Modifier.padding(end = 4.dp).size(12.dp)
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
                                                                favoriteViewModel.addFavorite(
                                                                    userUuid,
                                                                    popup.popupUuid
                                                                )
                                                            } else {
                                                                favoriteViewModel.deleteFavorite(
                                                                    userUuid,
                                                                    popup.popupUuid
                                                                )
                                                            }
                                                            favoriteViewModel.getFavoriteCount(userUuid,popup.popupUuid) { count ->
                                                                favoriteCount = count.toInt()
                                                            }
                                                        },
                                                        modifier = Modifier.padding(end = 4.dp).size(12.dp)
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

            if (showScrollToTop) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = Color.White,
                    shape = CircleShape,
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

        if (showDetail && detailPopup != null) {
            ContentDetail(
                popup = detailPopup!!,
                onClose = { setShowDetail(false) },
                loginResponse = loginResponse,
                favoriteViewModel = favoriteViewModel,
                showDetail = showDetail,
                setShowDetail = setShowDetail,
                selectPopupViewModel = selectPopupViewModel
            )
        }
    }
}

@Composable
fun CalendarTopBar(onAlarmClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = SpaceBetween
        ) {
            Text(
                text = "캘린더",
                style = Medium18,
                color = mainBlack,
            )
            IconButton(
                onClick = { onAlarmClick() },
            ) {
                Icon(
                    painter = painterResource(R.drawable.bell_icon),
                    contentDescription = "bell",
                    modifier = Modifier
                        .size(23.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
fun CustomCalendar(popupList: List<PopupEvent>,
                   selectedDate: LocalDate?,
                   onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    val displayedDate = remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val year = displayedDate.value.year
    val month = displayedDate.value.monthValue
    val firstDayOfMonth = displayedDate.value.withDayOfMonth(1)
    val lastDayOfMonth = firstDayOfMonth.lengthOfMonth()
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = SpaceBetween,
                    verticalAlignment = CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            displayedDate.value =
                                displayedDate.value.minusMonths(1).withDayOfMonth(1)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_down),
                            contentDescription = "이전 달",
                            modifier = Modifier
                                .size(16.dp)
                        )
                    }
                    Text(
                        text = "${year}년 ${month}월",
                        style = Medium17,
                        color = mainBlack
                    )
                    IconButton(
                        onClick = {
                            displayedDate.value =
                                displayedDate.value.plusMonths(1).withDayOfMonth(1)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_up),
                            contentDescription = "이후 달",
                            modifier = Modifier
                                .size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    daysOfWeek.forEach { day ->
                        Box(
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                style = Bold12,
                                color = mainGray2
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                var dayCounter = 1
                for (week in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = SpaceBetween
                    ) {
                        for (dayOfWeek in 0..6) {
                            val showDate =
                                (week == 0 && dayOfWeek < startDayOfWeek) || dayCounter > lastDayOfMonth
                            val currentDate =
                                if (!showDate) LocalDate.of(year, month, dayCounter) else null
                            val popupCount = if (!showDate && currentDate != null) {
                                popupList.count { popup ->
                                    val start = LocalDate.parse(popup.startDate)
                                    val end = LocalDate.parse(popup.endDate)
                                    !currentDate.isBefore(start) && !currentDate.isAfter(end)
                                }
                            } else 0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 15.dp)
                                    .clickable(enabled = !showDate) {
                                        if (!showDate && currentDate != null) onDateSelected(
                                            currentDate
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!showDate) {
                                    val isSelected = currentDate == selectedDate
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier =  if (isSelected)
                                                Modifier
                                                    .size(28.dp)
                                                    .background(color = mainOrange , shape = CircleShape)
                                            else Modifier.size(28.dp)
                                        ) {
                                            Text(
                                                text = dayCounter.toString(),
                                                style = Regular12,
                                                color = if (isSelected) Color.White else mainBlack,
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (popupCount > 0) {
                                            Text(
                                                text = "+${popupCount}건",
                                                style = Medium8,
                                                color = mainAmber
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "",
                                        style = Regular12,
                                        color = mainBlack
                                    )
                                }
                            }
                            if (!showDate) dayCounter++
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(31.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x05000000),
                            Color(0x0A000000)
                        )
                    )
                )
        )
    }
}

@Composable
fun CalendarLocalFilterButton(selectedRegion: String, onRegionSelected: (String) -> Unit, regionsViewModel: RegionsViewModel) {
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
            .clickable { showSheet = true }
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(0.dp, mainGray1, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row {
            Text(
                text = selectedRegion,
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
fun CalendarSortType(selectedSort: String, onSortSelected: (String) -> Unit) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sortItems = listOf(
        "최신순" to "NEWEST",
        "조회순" to "MOST_VIEWED",
        "마감임박순" to "CLOSING_SOON"
    )
    Box(modifier = Modifier
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
