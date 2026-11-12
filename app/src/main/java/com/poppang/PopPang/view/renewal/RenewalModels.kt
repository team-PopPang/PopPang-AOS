package com.poppang.PopPang.view.renewal

import com.poppang.PopPang.model.PopupEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

internal val RenewalCategories = listOf("디저트", "캐릭터", "뷰티", "패션", "전시", "굿즈", "아이돌", "라이프")

internal enum class PopupPeriod(
    val label: String,
) {
    All("전체"),
    Today("오늘"),
    ThisWeek("이번 주"),
    Custom("날짜 선택"),
}

internal data class RenewalPopup(
    val id: String,
    val name: String,
    val categories: List<String>,
    val region: String,
    val start: LocalDate,
    val end: LocalDate,
    val imageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isFavorited: Boolean = false,
) {
    fun status(today: LocalDate): String =
        when {
            today.isBefore(start) -> "오픈 예정"
            today.isAfter(end) -> "종료"
            else -> "운영중"
        }
}

internal data class RenewalMapFilters(
    val categories: Set<String> = emptySet(),
    val period: PopupPeriod = PopupPeriod.All,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
) {
    val activeCount: Int
        get() = categories.size + (if (period != PopupPeriod.All) 1 else 0)

    fun apply(
        popups: List<RenewalPopup>,
        query: String,
        today: LocalDate,
    ): List<RenewalPopup> {
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val range =
            when (period) {
                PopupPeriod.Today -> today to today
                PopupPeriod.ThisWeek -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) to weekEnd
                PopupPeriod.Custom -> if (start != null && end != null) start to end else null
                PopupPeriod.All -> null
            }
        val search = query.trim()
        return popups.filter { popup ->
            val categoryMatches = categories.isEmpty() || popup.categories.any { it in categories }
            val notExpired = !popup.end.isBefore(today)
            val periodMatches = range == null || (!popup.start.isAfter(range.second) && !popup.end.isBefore(range.first))
            val queryMatches =
                search.isEmpty() || (listOf(popup.name, popup.region) + popup.categories).any { it.contains(search, ignoreCase = true) }
            categoryMatches && notExpired && periodMatches && queryMatches
        }
    }
}

// The first slot held the old status filter. Ignore it when restoring existing sessions.
internal fun restoreRenewalMapFilters(values: List<String>): RenewalMapFilters =
    RenewalMapFilters(
        categories = values.drop(4).toSet(),
        period = PopupPeriod.valueOf(values[1]),
        start = values[2].takeIf(String::isNotEmpty)?.let(LocalDate::parse),
        end = values[3].takeIf(String::isNotEmpty)?.let(LocalDate::parse),
    )

internal fun PopupEvent.toRenewalPopup(): RenewalPopup? {
    val start = runCatching { LocalDate.parse(startDate.take(10)) }.getOrNull() ?: return null
    val end = runCatching { LocalDate.parse(endDate.take(10)) }.getOrNull() ?: return null
    return RenewalPopup(
        popupUuid,
        name,
        recommendList,
        region,
        start,
        end,
        fullImageUrlList.firstOrNull(),
        latitude,
        longitude,
        isFavorited,
    )
}

internal fun renewalToday(): LocalDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
