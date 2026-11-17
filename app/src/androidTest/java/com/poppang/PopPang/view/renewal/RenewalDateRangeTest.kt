package com.poppang.PopPang.view.renewal

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class RenewalDateRangeTest {
    @get:Rule val compose = createComposeRule()
    private val today = LocalDate.of(2026, 11, 3)

    @Test fun twoDatesConfirmTheRangeAndCloseTheCalendar() {
        val visible = mutableStateOf(true)
        var result: Pair<LocalDate, LocalDate>? = null
        compose.setContent {
            PopPangAOSTheme(false) {
                if (visible.value) {
                    RenewalDateRangeDialog(null, null, { visible.value = false }, today) { start, end ->
                        result = start to end
                        visible.value = false
                    }
                }
            }
        }
        compose.onNodeWithText("2026년 11월").assertIsDisplayed()
        compose.onNodeWithTag("date-2026-11-08").performClick().assertIsSelected()
        compose.runOnIdle { assertNull(result) }
        compose.onNodeWithTag("date-2026-11-12").performClick()
        compose.runOnIdle { assertEquals(LocalDate.of(2026, 11, 8) to LocalDate.of(2026, 11, 12), result) }
        compose.onNodeWithTag("map-date-picker").assertDoesNotExist()
    }

    @Test fun anEarlierSecondTapRestartsTheRangeAndTheSameDayCanBeSelected() {
        var result: Pair<LocalDate, LocalDate>? = null
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalDateRangeDialog(null, null, {}, today) { start, end -> result = start to end }
            }
        }
        compose.onNodeWithTag("date-2026-11-12").performClick()
        compose.onNodeWithTag("date-2026-11-08").performClick().assertIsSelected()
        compose.runOnIdle { assertNull(result) }
        compose.onNodeWithTag("date-2026-11-12").assertIsNotSelected()
        compose.onNodeWithTag("date-2026-11-08").performClick()
        compose.runOnIdle { assertEquals(LocalDate.of(2026, 11, 8) to LocalDate.of(2026, 11, 8), result) }
    }

    @Test fun aPendingRangeSurvivesMonthNavigationAndStateRestoration() {
        var result: Pair<LocalDate, LocalDate>? = null
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            PopPangAOSTheme(false) {
                RenewalDateRangeDialog(null, null, {}, today) { start, end -> result = start to end }
            }
        }
        compose.onNodeWithTag("date-2026-11-30").performClick()
        compose.onNodeWithContentDescription("다음 달").performClick()
        compose.onNodeWithText("2026년 12월").assertIsDisplayed()
        compose.onNodeWithContentDescription("이전 달").performClick()
        compose.onNodeWithTag("date-2026-11-30").assertIsSelected()
        compose.onNodeWithContentDescription("다음 달").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("2026년 12월").assertIsDisplayed()
        compose.onNodeWithTag("date-2026-12-02").performClick()
        compose.runOnIdle { assertEquals(LocalDate.of(2026, 11, 30) to LocalDate.of(2026, 12, 2), result) }
    }

    @Test fun reopeningAnAppliedRangeHighlightsBothEndpointsAndTheDaysBetween() {
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalDateRangeDialog(LocalDate.of(2026, 11, 8), LocalDate.of(2026, 11, 12), {}, today) { _, _ -> }
            }
        }
        (8..12).forEach { day -> compose.onNodeWithTag("date-2026-11-${day.toString().padStart(2, '0')}").assertIsSelected() }
        compose.onNodeWithTag("date-2026-11-07").assertIsNotSelected()
        compose.onNodeWithTag("date-2026-11-13").assertIsNotSelected()
    }

    @Test fun closingAPartialSelectionKeepsThePreviouslyAppliedRange() {
        val original = LocalDate.of(2026, 11, 8) to LocalDate.of(2026, 11, 12)
        var applied = original
        val visible = mutableStateOf(true)
        compose.setContent {
            PopPangAOSTheme(false) {
                if (visible.value) {
                    RenewalDateRangeDialog(original.first, original.second, { visible.value = false }, today) { start, end ->
                        applied = start to end
                    }
                }
            }
        }
        compose.onNodeWithTag("date-2026-11-20").performClick()
        Espresso.pressBack()
        compose.onNodeWithTag("map-date-picker").assertDoesNotExist()
        compose.runOnIdle { assertEquals(original, applied) }
    }
}
