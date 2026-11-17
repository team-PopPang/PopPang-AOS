package com.poppang.PopPang.view.renewal

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FilterMenuMotionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun optionsExpandDownAndFoldUpWhileChipsStayInPlace() {
        compose.setContent {
            PopPangAOSTheme(false) {
                FilterShortcuts(RenewalMapFilters(), (1..20).map { "항목 $it" }, {}, {})
            }
        }
        val chip = compose.onNodeWithTag("map-filter-chip-카테고리")
        val chipBounds = chip.getUnclippedBoundsInRoot()
        val motion = compose.onNodeWithTag("map-filter-motion-카테고리")

        fun menuHeight(): Float {
            val bounds = motion.getUnclippedBoundsInRoot()
            return bounds.bottom.value - bounds.top.value
        }
        compose.mainClock.autoAdvance = false
        try {
            chip.performClick()
            compose.mainClock.advanceTimeByFrame()
            compose.mainClock.advanceTimeBy(64)
            val opening = menuHeight()
            assertEquals(chipBounds, chip.getUnclippedBoundsInRoot())
            compose.mainClock.advanceTimeBy(300)
            val fullHeight = menuHeight()
            assertTrue("Opening must reveal an intermediate height", opening > 0f && opening < fullHeight)
            Espresso.pressBack()
            compose.mainClock.advanceTimeByFrame()
            compose.mainClock.advanceTimeBy(64)
            val closing = menuHeight()
            assertTrue("Closing must retain the panel until the upward fold finishes", closing > 0f && closing < fullHeight)
            assertEquals(chipBounds, chip.getUnclippedBoundsInRoot())
            compose.mainClock.advanceTimeBy(300)
            motion.assertDoesNotExist()
            // Closing before the opening finishes must also fold back rather than vanish.
            chip.performClick()
            compose.mainClock.advanceTimeByFrame()
            compose.mainClock.advanceTimeBy(64)
            val interrupted = menuHeight()
            assertTrue(interrupted > 0f)
            Espresso.pressBack()
            compose.mainClock.advanceTimeByFrame()
            motion.assertExists()
            compose.mainClock.advanceTimeBy(64)
            assertTrue("An interrupted opening must fold back from its current height", menuHeight() < interrupted)
            compose.mainClock.advanceTimeBy(300)
            motion.assertDoesNotExist()
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test fun aClosingMenuCannotApplyAndAQuickReopenKeepsOnePanel() {
        var applied = 0
        compose.setContent {
            PopPangAOSTheme(false) {
                FilterShortcuts(RenewalMapFilters(), listOf("디저트", "캐릭터"), { applied++ }, {})
            }
        }
        compose.onNodeWithText("카테고리").performClick()
        compose.onNodeWithText("디저트 (0)").performClick()
        compose.onNodeWithText("1개 적용").assertExists()
        compose.mainClock.autoAdvance = false
        try {
            Espresso.pressBack()
            compose.mainClock.advanceTimeByFrame()
            compose.mainClock.advanceTimeBy(64)
            compose.onNodeWithTag("map-filter-motion-카테고리").assertExists()
            compose.onNodeWithText("1개 적용").assertDoesNotExist()
            compose.onNodeWithTag("map-filter-chip-카테고리").performClick()
            compose.mainClock.advanceTimeBy(400)
            compose.onAllNodesWithTag("map-filter-motion-카테고리").assertCountEquals(1)
            compose.onNodeWithText("0개 적용").assertExists()
            compose.runOnIdle { assertEquals(0, applied) }
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }
}
