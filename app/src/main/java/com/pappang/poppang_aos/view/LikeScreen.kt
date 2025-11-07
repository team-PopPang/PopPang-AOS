package com.pappang.poppang_aos.view

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.model.LoginResponse
import com.pappang.poppang_aos.model.PopupEvent
import com.pappang.poppang_aos.ui.theme.Medium10
import com.pappang.poppang_aos.ui.theme.Medium12
import com.pappang.poppang_aos.ui.theme.Medium17
import com.pappang.poppang_aos.ui.theme.Medium18
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.mainAmber
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray2
import com.pappang.poppang_aos.ui.theme.mainGray3
import com.pappang.poppang_aos.ui.theme.mainGray5
import com.pappang.poppang_aos.ui.theme.mainRed
import java.time.LocalDate

@Composable
fun LikeScreen(
    hideSatausBar: (Boolean) -> Unit = {},
    popupList: List<PopupEvent>,
    showDetail: Boolean = false,
    setShowDetail: (Boolean) -> Unit,
    showAlarm: Boolean = false,
    setShowAlarm: (Boolean) -> Unit,
    loginResponse: LoginResponse?
) {
    val selectedIndex = remember { mutableStateOf(0) }
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
            LikeTopBar(onAlarmClick = { setShowAlarm(true) })
            LikeItem(selectedIndex = selectedIndex)
            when (selectedIndex.value) {
                0 -> LikeActivityTab()
                1 -> LikeCalendarTab(
                    popupList = popupList,
                    selectedDate = selectedDate.value,
                    onDateSelected = { selectedDate.value = it })
            }
        }
    }
}

@Composable
fun LikeTopBar(onAlarmClick: () -> Unit) {
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
                text = "찜",
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
fun LikeItem( selectedIndex: MutableState<Int>) {

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
                    text = "찜리스트",
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
                    text = "찜캘린더",
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
fun LikeActivityTab() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "찜한 팝업스토어가 없어요.",
                style = Medium12,
                color = mainBlack,
                modifier = Modifier
                    .padding(bottom = 10.dp)
            )
            CustomButton4(
                onClick = { /* TODO: Navigate to popup store list */ },
                text = "팝업스토어 구경하기",
                modifier = Modifier
                    .padding(start = 49.dp, end = 49.dp, top = 8.dp, bottom = 8.dp)
                    .size(width = 206.dp, height = 33.dp)
            )
        }
    }
}

@Composable
fun LikeCalendarTab(popupList: List<PopupEvent>,
                    selectedDate: LocalDate?,
                    onDateSelected: (LocalDate) -> Unit
){
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
@Preview
fun LikeScreenPreview() {
    LikeActivityTab()
}