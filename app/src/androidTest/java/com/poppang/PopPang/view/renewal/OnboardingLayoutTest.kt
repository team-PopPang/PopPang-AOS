package com.poppang.PopPang.view.renewal

import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import com.poppang.PopPang.view.OnboardingScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun skippingCannotFinishTwiceWhileItsScreenIsLeaving() {
        var finished = 0
        compose.setContent { PopPangAOSTheme(false) { OnboardingScreen { finished++ } } }
        compose.onNodeWithText("건너뛰기").performClick()
        compose.onNodeWithText("건너뛰기").assertIsNotEnabled().performClick()
        compose.onNodeWithTag("onboarding-next").performClick()
        compose.runOnIdle { assertEquals(1, finished) }
        compose.onNodeWithText("놓치기 싫은 팝업,\n키워드만 등록하세요").assertIsDisplayed()
    }

    @Test fun theButtonHasBottomSpaceAndTheLastPageHasNoSkipAction() {
        var finished = 0
        compose.setContent { PopPangAOSTheme(false) { OnboardingScreen { finished++ } } }
        val button = compose.onNodeWithTag("onboarding-next")
        val original = button.getUnclippedBoundsInRoot()
        val screen = compose.onNodeWithTag("onboarding-screen").getUnclippedBoundsInRoot()
        button.assertHeightIsEqualTo(52.dp)
        assertTrue(
            "The button must leave at least 16dp above the system navigation area",
            screen.bottom.value - original.bottom.value >= 15.5f,
        )
        compose.onNodeWithText("건너뛰기").assertIsDisplayed()
        button.performClick()
        compose.onNodeWithText("건너뛰기").assertIsDisplayed()
        button.performClick()
        compose.onNodeWithText("건너뛰기").assertDoesNotExist()
        compose.onNodeWithText("시작하기").assertIsDisplayed()
        assertEquals(original, button.getUnclippedBoundsInRoot())
        Espresso.pressBack()
        compose.onNodeWithText("건너뛰기").assertIsDisplayed()
        button.performClick()
        compose.onNodeWithText("건너뛰기").assertDoesNotExist()
        button.performClick()
        compose.runOnIdle { assertEquals(1, finished) }
    }

    @Test fun pageContentSlidesWhileTheButtonStaysFixedAndTheOutgoingSkipCannotFinish() {
        var finished = 0
        compose.setContent { PopPangAOSTheme(false) { OnboardingScreen { finished++ } } }
        val button = compose.onNodeWithTag("onboarding-next")
        val original = button.getUnclippedBoundsInRoot()
        compose.mainClock.autoAdvance = false
        try {
            button.performClick()
            compose.mainClock.advanceTimeByFrame()
            compose.mainClock.advanceTimeBy(64)
            val nextTitle = compose.onNodeWithText("지금 근처에서 열린 팝업,\n지도로 한눈에")
            val movingLeft = nextTitle.getUnclippedBoundsInRoot().left.value
            assertEquals(original, button.getUnclippedBoundsInRoot())
            compose.mainClock.advanceTimeBy(400)
            val settledLeft = nextTitle.getUnclippedBoundsInRoot().left.value
            assertTrue("The next page must slide in from the right", movingLeft > settledLeft + 1f)
            compose.onNodeWithText("놓치기 싫은 팝업,\n키워드만 등록하세요").assertDoesNotExist()
            button.performClick()
            compose.mainClock.advanceTimeByFrame()
            compose.mainClock.advanceTimeBy(32)
            compose.onNodeWithText("건너뛰기").assertIsNotEnabled().performClick()
            compose.runOnIdle { assertEquals(0, finished) }
            compose.mainClock.advanceTimeBy(400)
            compose.onNodeWithText("건너뛰기").assertDoesNotExist()
            assertEquals(original, button.getUnclippedBoundsInRoot())
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }
}
