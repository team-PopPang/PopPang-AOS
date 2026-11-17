package com.poppang.PopPang.view.renewal

import com.poppang.PopPang.model.PopupEvent
import java.time.LocalDate
import java.time.ZoneId

internal val RenewalCategories = listOf("디저트", "캐릭터", "뷰티", "패션", "전시", "굿즈", "아이돌", "라이프")

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
) {
    val activeCount: Int
        get() = categories.size

    fun apply(
        popups: List<RenewalPopup>,
        query: String,
        today: LocalDate,
    ): List<RenewalPopup> {
        val search = query.trim()
        return popups.filter { popup ->
            val categoryMatches = categories.isEmpty() || popup.categories.any { it in categories }
            val notExpired = !popup.end.isBefore(today)
            val queryMatches =
                search.isEmpty() || (listOf(popup.name, popup.region) + popup.categories).any { it.contains(search, ignoreCase = true) }
            categoryMatches && notExpired && queryMatches
        }
    }
}

// Preserve category positions while ignoring the removed status and period filters.
internal fun restoreRenewalMapFilters(values: List<String>): RenewalMapFilters = RenewalMapFilters(categories = values.drop(4).toSet())

internal fun renewalCategoryCounts(
    popups: List<RenewalPopup>,
    query: String,
    today: LocalDate,
): Map<String, Int> =
    RenewalMapFilters()
        .apply(popups, query, today)
        .flatMap { it.categories.distinct() }
        .groupingBy { it }
        .eachCount()

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
