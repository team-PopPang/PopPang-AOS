package com.pappang.poppang_aos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.R
import com.pappang.poppang_aos.ui.theme.Medium17
import com.pappang.poppang_aos.ui.theme.Regular12
import com.pappang.poppang_aos.ui.theme.mainBlack
import com.pappang.poppang_aos.ui.theme.mainGray2


@Composable
fun CalendarScreen() {
    CustomCalendar()
}
@Composable
fun CustomCalendar() {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    val today = java.time.LocalDate.now()
    val year = today.year
    val month = today.monthValue
    val firstDayOfMonth = java.time.LocalDate.of(year, month, 1)
    val lastDayOfMonth = firstDayOfMonth.lengthOfMonth()
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* 이전 달로 이동 */ }
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
                    onClick = { /* 이후 달로 이동 */ }
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
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (showDate) "" else dayCounter.toString(),
                                style = Regular12,
                                color = mainBlack
                            )
                        }
                        if (!showDate) dayCounter++
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun CalendarScreenPreview() {
    CalendarScreen()
}