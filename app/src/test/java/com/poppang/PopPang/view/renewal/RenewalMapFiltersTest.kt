package com.poppang.PopPang.view.renewal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RenewalMapFiltersTest {
    private val today = LocalDate.of(2026, 11, 17)
    private val ongoing = RenewalPopup("ongoing", "디저트 팝업", listOf("디저트"), "울산 남구", today.minusDays(2), today.plusDays(2))
    private val upcoming = RenewalPopup("upcoming", "CHARACTER 팝업", listOf("캐릭터"), "울산 남구", today.plusDays(1), today.plusDays(10))
    private val endsToday = ongoing.copy(id = "endsToday", end = today)
    private val ended = upcoming.copy(id = "ended", start = today.minusDays(7), end = today.minusDays(1))
    private val all = listOf(ongoing, upcoming, endsToday, ended)

    @Test fun defaultsIncludeUpcomingAndOngoingButExcludeExpired() {
        assertEquals(listOf(ongoing, upcoming, endsToday), RenewalMapFilters().apply(all, "", today))
    }

    @Test fun categoriesUseOrMatchingAndCombineWithTheSearchQuery() {
        val filters = RenewalMapFilters(categories = setOf("캐릭터", "굿즈"))
        assertEquals(listOf(upcoming), filters.apply(all, "울산", today))
        assertTrue(filters.apply(all, "없는 검색어", today).isEmpty())
    }

    @Test fun queryTrimsSpacesAndIgnoresCase() {
        assertEquals(listOf(upcoming), RenewalMapFilters().apply(all, " character ", today))
    }

    @Test fun searchUsesThePopupNameOrAddress() {
        val popup = ongoing.copy(name = "여름 팝업", region = "서울", address = "서울 성동구 연무장길", categories = listOf("뷰티"))
        assertEquals(listOf(popup), RenewalMapFilters().apply(listOf(popup), "연무장길", today))
        assertTrue(RenewalMapFilters().apply(listOf(popup), "뷰티", today).isEmpty())
    }

    @Test fun countsIncludeEachEligiblePopupInItsCategory() {
        assertEquals(mapOf("디저트" to 2, "캐릭터" to 1), renewalCategoryCounts(all, "", today))
        assertTrue(renewalCategoryCounts(emptyList(), "", today).isEmpty())
    }

    @Test fun multipleCategoriesCountOnceEachEvenIfAServerCategoryIsRepeated() {
        val multiple = ongoing.copy(id = "multiple", categories = listOf("디저트", "디저트", "캐릭터"))
        assertEquals(mapOf("디저트" to 3, "캐릭터" to 2), renewalCategoryCounts(all + multiple, "", today))
    }

    @Test fun countsRespectTheCurrentSearchAndRefreshWithTheLoadedData() {
        assertEquals(mapOf("캐릭터" to 1), renewalCategoryCounts(all, "CHARACTER", today))
        assertTrue(renewalCategoryCounts(all, "없는 검색어", today).isEmpty())
        assertEquals(mapOf("캐릭터" to 1), renewalCategoryCounts(listOf(upcoming), "", today))
    }

    @Test fun restoringLegacyStatusAndPeriodValuesKeepsOnlyTheSelectedCategories() {
        for (oldStatus in listOf("All", "InProgress", "Upcoming", "ClosingThisWeek")) {
            for (oldPeriod in listOf("All", "Today", "ThisWeek", "Custom", "Unknown")) {
                val filters =
                    restoreRenewalMapFilters(
                        listOf(oldStatus, oldPeriod, "old-start", "old-end", "디저트", "캐릭터"),
                    )
                assertEquals(setOf("디저트", "캐릭터"), filters.categories)
                assertEquals(2, filters.activeCount)
                assertEquals(listOf(ongoing, upcoming, endsToday), filters.apply(all, "", today))
            }
        }
    }

    @Test fun restoringAnEmptySelectionDoesNotKeepAnyHiddenFilter() {
        val filters = restoreRenewalMapFilters(listOf("Upcoming", "Today", "", ""))
        assertEquals(0, filters.activeCount)
        assertEquals(listOf(ongoing, upcoming, endsToday), filters.apply(all, "", today))
    }
}
