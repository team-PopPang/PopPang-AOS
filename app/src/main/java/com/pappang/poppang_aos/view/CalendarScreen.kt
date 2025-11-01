package com.pappang.poppang_aos.view

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.model.PopupEvent
import com.pappang.poppang_aos.ui.theme.Bold15
import com.pappang.poppang_aos.ui.theme.Medium10
import com.pappang.poppang_aos.ui.theme.Medium12
import com.pappang.poppang_aos.ui.theme.Medium17
import com.pappang.poppang_aos.ui.theme.Medium18
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.mainAmber
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray2
import com.pappang.poppang_aos.ui.theme.mainGray5
import com.pappang.poppang_aos.ui.theme.mainRed
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale.KOREAN


@Composable
fun CalendarScreen(
    hideSatausBar: (Boolean) -> Unit = {},
    popupList: List<PopupEvent>,
    showDetail: Boolean = false,
    setShowDetail: (Boolean) -> Unit,
    showAlarm: Boolean = false,
    setShowAlarm: (Boolean) -> Unit,
    loginResponse: LoginResponse?) {
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    var selectedPopup by remember { mutableStateOf<PopupEvent?>(null) }
    if (showAlarm) {
        AlarmScreen(onClose = { setShowAlarm(false) }, loginResponse = loginResponse)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            CalendarTopBar(onAlarmClick = { setShowAlarm(true) })
            CustomCalendar(
                popupList = popupList,
                selectedDate = selectedDate.value,
                onDateSelected = { selectedDate.value = it }
            )
            CalendarContent(
                popupList = popupList,
                selectedDate = selectedDate.value,
                onShowDetail = { popup ->
                    selectedPopup = popup
                    setShowDetail(true)
                }
            )
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
            horizontalArrangement = Arrangement.SpaceBetween
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
                        .padding(start = 15.dp)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                                style = Regular12,
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
                        horizontalArrangement = Arrangement.SpaceBetween
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
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayCounter.toString(),
                                            style = Regular12,
                                            color = mainBlack
                                        )
                                        if (popupCount > 0) {
                                            Text(
                                                text = "+${popupCount}건",
                                                style = Medium10,
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
fun CalendarContent(
    popupList: List<PopupEvent>,
    selectedDate: LocalDate?,
    onShowDetail: (PopupEvent) -> Unit
) {
    val filteredList = if (selectedDate != null) {
        popupList.filter { popup ->
            val start = LocalDate.parse(popup.startDate)
            val end = LocalDate.parse(popup.endDate)
            !selectedDate.isBefore(start) && !selectedDate.isAfter(end)
        }
    } else emptyList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (selectedDate != null) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                ){
                    Text(
                        text = "${selectedDate.dayOfMonth}일",
                        style = Medium12,
                        color = mainBlack,
                    )
                    Text(
                        text = "${selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, KOREAN)}요일",
                        style = Medium12,
                        color = mainBlack,
                        modifier = Modifier
                            .padding(start = 5.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LazyColumn {
                    items(filteredList) { popup ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(163.dp)
                                .padding(vertical = 12.dp)
                                .clickable { onShowDetail(popup) }
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
                                                    text = popup.roadAddress.split(" ").take(2)
                                                        .joinToString(" "),
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
    }
}