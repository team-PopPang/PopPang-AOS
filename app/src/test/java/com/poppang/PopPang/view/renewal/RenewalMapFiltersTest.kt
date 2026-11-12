package com.poppang.PopPang.view.renewal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RenewalMapFiltersTest {
    private val today = LocalDate.of(2026, 11, 3)

    private fun popup(
        id: String,
        start: LocalDate,
        end: LocalDate,
        category: String = "디저트",
    ) = RenewalPopup(id, "팝업 $id", listOf(category), "울산 남구", start, end)

    private val ongoing = popup("ongoing", today.minusDays(2), today.plusDays(2))
    private val upcoming = popup("upcoming", today.plusDays(1), today.plusDays(10), "캐릭터")
    private val endsToday = popup("endsToday", today.minusDays(5), today)
    private val ended = popup("ended", today.minusDays(7), today.minusDays(1))
    private val all = listOf(ongoing, upcoming, endsToday, ended)

    @Test fun defaultsIncludeUpcomingAndOngoingButExcludeExpired() {
        assertEquals(listOf("ongoing", "upcoming", "endsToday"), RenewalMapFilters().apply(all, "", today).map { it.id })
    }

    @Test fun categoryAndPeriodAndQueryAreCombined() {
        val filters =
            RenewalMapFilters(
                categories = setOf("캐릭터", "굿즈"),
                period = PopupPeriod.Custom,
                start = today.plusDays(1),
                end = today.plusDays(4),
            )
        assertEquals(listOf(upcoming), filters.apply(all, "울산", today))
        assertTrue(filters.apply(all, "없는 검색어", today).isEmpty())
    }

    @Test fun customRangeUsesInclusiveOverlap() {
        val filters = RenewalMapFilters(period = PopupPeriod.Custom, start = today.plusDays(2), end = today.plusDays(4))
        assertEquals(listOf(ongoing, upcoming), filters.apply(all, "", today))
    }

    @Test fun todayIncludesOpeningAndClosingDaysButNotTomorrow() {
        val opensToday = popup("opensToday", today, today.plusDays(3))
        assertEquals(
            listOf(ongoing, endsToday, opensToday),
            RenewalMapFilters(period = PopupPeriod.Today).apply(all + opensToday, "", today),
        )
    }

    @Test fun thisWeekIncludesOverlappingPopupsAndExcludesNextWeek() {
        val nextWeek = popup("nextWeek", today.plusWeeks(1), today.plusWeeks(2))
        assertEquals(
            listOf(ongoing, upcoming, endsToday),
            RenewalMapFilters(period = PopupPeriod.ThisWeek).apply(all + nextWeek, "", today),
        )
    }

    @Test fun impossibleFilterCombinationKeepsAnEmptyResult() {
        assertTrue(RenewalMapFilters(categories = setOf("캐릭터"), period = PopupPeriod.Today).apply(all, "", today).isEmpty())
    }

    @Test fun restoringLegacyStatusesPreservesCategoriesAndDatesWithoutAHiddenFilter() {
        for (oldStatus in listOf("All", "InProgress", "Upcoming", "ClosingThisWeek")) {
            val filters =
                restoreRenewalMapFilters(
                    listOf(oldStatus, "Custom", today.minusDays(1).toString(), today.plusDays(10).toString(), "디저트", "캐릭터"),
                )
            assertEquals(setOf("디저트", "캐릭터"), filters.categories)
            assertEquals(PopupPeriod.Custom, filters.period)
            assertEquals(today.minusDays(1), filters.start)
            assertEquals(today.plusDays(10), filters.end)
            assertEquals(3, filters.activeCount)
            assertEquals(listOf(ongoing, upcoming, endsToday), filters.apply(all, "", today))
        }
    }
}
