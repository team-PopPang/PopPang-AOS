package com.poppang.PopPang.view.renewal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.poppang.PopPang.R
import com.poppang.PopPang.ui.theme.mainOrange
import java.time.LocalDate
import java.time.YearMonth

private val CalendarInk = Color(0xFF515D75)

@Composable
internal fun RenewalDateRangeDialog(
    start: LocalDate?,
    end: LocalDate?,
    onDismiss: () -> Unit,
    today: LocalDate = renewalToday(),
    onConfirm: (LocalDate, LocalDate) -> Unit,
) {
    var firstDay by rememberSaveable(start, end) { mutableStateOf(start?.toEpochDay()) }
    var lastDay by rememberSaveable(start, end) { mutableStateOf(end?.toEpochDay()) }
    var monthKey by rememberSaveable(start) { mutableStateOf(YearMonth.from(start ?: today).toString()) }
    val month = YearMonth.parse(monthKey)
    val rangeStart = firstDay?.let(LocalDate::ofEpochDay)
    val rangeEnd = lastDay?.let(LocalDate::ofEpochDay)
    val leadingDays = month.atDay(1).dayOfWeek.value % 7
    val weeks = (leadingDays + month.lengthOfMonth() + 6) / 7

    fun selectDate(date: LocalDate) {
        val currentStart = firstDay
        if (currentStart == null || lastDay != null || date.toEpochDay() < currentStart) {
            firstDay = date.toEpochDay()
            lastDay = null
        } else {
            lastDay = date.toEpochDay()
            onConfirm(LocalDate.ofEpochDay(currentStart), date)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
            Surface(
                modifier =
                    Modifier
                        .widthIn(max = 320.dp)
                        .fillMaxWidth()
                        .testTag("map-date-picker")
                        .semantics { paneTitle = "날짜 선택" },
                shape = RectangleShape,
                color = Color.White,
            ) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${month.year}년 ${month.monthValue}월",
                            style = renewalNotoText(12),
                            color = CalendarInk,
                            modifier = Modifier.weight(1f).testTag("date-month"),
                        )
                        MonthArrow("이전 달", 90f, month.year > 1900 || month.monthValue > 1) {
                            monthKey = month.minusMonths(1).toString()
                        }
                        MonthArrow("다음 달", -90f, month.year < 2100 || month.monthValue < 12) {
                            monthKey = month.plusMonths(1).toString()
                        }
                    }
                    Row(Modifier.fillMaxWidth().heightIn(min = 24.dp)) {
                        listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { day, label ->
                            Box(Modifier.weight(1f).heightIn(min = 24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    label,
                                    style = renewalNotoText(9),
                                    color =
                                        when (day) {
                                            0 -> Color(0xFFF08B9E)
                                            6 -> Color(0xFF74A0F9)
                                            else -> Color(0xFF8D97A9)
                                        },
                                )
                            }
                        }
                    }
                    repeat(weeks) { week ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            repeat(7) { column ->
                                val day = week * 7 + column - leadingDays + 1
                                if (day in 1..month.lengthOfMonth()) {
                                    CalendarDay(
                                        date = month.atDay(day),
                                        start = rangeStart,
                                        end = rangeEnd,
                                        today = today,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectDate(month.atDay(day)) },
                                    )
                                } else {
                                    Box(Modifier.weight(1f).height(36.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthArrow(
    label: String,
    rotation: Float,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(28.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        RenewalIcon(
            R.drawable.renewal_asset_b7881,
            size = 14.dp,
            modifier = Modifier.rotate(rotation).alpha(if (enabled) 1f else .35f),
        )
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    start: LocalDate?,
    end: LocalDate?,
    today: LocalDate,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val isStart = date == start
    val isEnd = date == end
    val endpoint = isStart || isEnd
    val inRange = start != null && end != null && !date.isBefore(start) && !date.isAfter(end)
    Box(
        modifier
            .heightIn(min = 36.dp)
            .testTag("date-$date")
            .semantics {
                contentDescription = "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일"
                selected = endpoint || inRange
                stateDescription =
                    when {
                        isStart && isEnd -> "시작일 및 종료일"
                        isStart -> "시작일"
                        isEnd -> "종료일"
                        inRange -> "선택 기간"
                        date == today -> "오늘"
                        else -> "선택 안 함"
                    }
            }.clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (inRange && start != end) {
            Box(
                Modifier
                    .align(if (isStart) Alignment.CenterEnd else Alignment.CenterStart)
                    .fillMaxWidth(if (endpoint) .5f else 1f)
                    .height(32.dp)
                    .background(RenewalPeach),
            )
        }
        Box(
            Modifier.size(32.dp).background(if (endpoint) mainOrange else Color.Transparent, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                date.dayOfMonth.toString(),
                style =
                    TextStyle(
                        fontFamily = RenewalInter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        lineHeightStyle = RenewalLineHeight,
                    ),
                color = if (endpoint) Color.White else CalendarInk,
            )
        }
    }
}
