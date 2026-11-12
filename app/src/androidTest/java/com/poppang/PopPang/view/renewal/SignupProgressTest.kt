package com.poppang.PopPang.view.renewal

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignupProgressTest {
    @get:Rule val compose = createComposeRule()

    @Test fun anOutgoingKeywordStepCannotSubmitAfterGoingBack() {
        var submissions = 0
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    "설레는수달",
                    {},
                    "",
                    true,
                    false,
                    {},
                    RenewalCategories,
                    listOf("디저트"),
                    {},
                    emptyList(),
                    {},
                    {},
                    onSubmit = { submissions++ },
                    onStart = {},
                )
            }
        }
        compose.onNodeWithTag("signup-next").performClick().performClick()
        compose.mainClock.autoAdvance = false
        try {
            compose.onNodeWithContentDescription("뒤로가기").performClick()
            compose.mainClock.advanceTimeByFrame()
            compose.onNodeWithText("건너뛰기").assertIsNotEnabled().performClick()
            compose.runOnIdle { assertEquals(0, submissions) }
            compose.mainClock.advanceTimeBy(600)
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test fun changingStepsAnimatesTheFillAndFadesContentWithoutMovingTheButton() {
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    "설레는수달",
                    {},
                    "",
                    true,
                    false,
                    {},
                    RenewalCategories,
                    listOf("디저트"),
                    {},
                    emptyList(),
                    {},
                    {},
                    onSubmit = {},
                    onStart = {},
                )
            }
        }
        val buttonBounds = compose.onNodeWithTag("signup-next").getUnclippedBoundsInRoot()
        val before = darkContentPixels()
        compose.mainClock.autoAdvance = false
        try {
            compose.onNodeWithTag("signup-next").performClick()
            compose.mainClock.advanceTimeBy(150)
            val progress =
                compose
                    .onNodeWithTag("signup-progress")
                    .fetchSemanticsNode()
                    .config[SemanticsProperties.ProgressBarRangeInfo]
                    .current
            assertTrue("The fill should be between the old and new steps", progress > 1f / 3f && progress < 2f / 3f)
            assertEquals(buttonBounds, compose.onNodeWithTag("signup-next").getUnclippedBoundsInRoot())
            val fading = darkContentPixels()
            assertTrue("The outgoing content should softly fade before the new content settles", fading < before / 2)
            compose.mainClock.advanceTimeBy(600)
            compose.onNodeWithTag("signup-title").assertTextEquals("어떤 팝업을\n좋아하세요?")
            assertTrue("The incoming content should become visible", darkContentPixels() > fading)
            assertEquals(buttonBounds, compose.onNodeWithTag("signup-next").getUnclippedBoundsInRoot())
            val settled =
                compose
                    .onNodeWithTag("signup-progress")
                    .fetchSemanticsNode()
                    .config[SemanticsProperties.ProgressBarRangeInfo]
                    .current
            assertEquals(2f / 3f, settled, 0.005f)
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    private fun darkContentPixels(): Int {
        val pixels = compose.onNodeWithTag("signup-content").captureToImage().toPixelMap()
        var count = 0
        for (y in 0 until minOf(pixels.height, pixels.width / 3)) {
            for (x in 0 until pixels.width) {
                val color = pixels[x, y]
                if (color.red < 0.3f && color.green < 0.3f && color.blue < 0.3f) count++
            }
        }
        return count
    }

    @Test fun theFillAndTrackStayConnectedThroughAllSignupSteps() {
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalSignupFlow(
                    "설레는수달",
                    {},
                    "",
                    true,
                    false,
                    {},
                    RenewalCategories,
                    listOf("디저트"),
                    {},
                    emptyList(),
                    {},
                    {},
                    onSubmit = {},
                    onStart = {},
                )
            }
        }
        repeat(3) { step ->
            val pixels = compose.onNodeWithTag("signup-progress").captureToImage().toPixelMap()
            val y = pixels.height / 2
            val orange =
                (0 until pixels.width).filter { x ->
                    val color = pixels[x, y]
                    color.red > 0.95f && color.green >= 0.35f && color.green <= 0.6f && color.blue < 0.05f
                }
            assertTrue("Step ${step + 1} must show the orange fill", orange.isNotEmpty())
            val lastOrange = orange.last()
            val whiteGap =
                (lastOrange + 1 until pixels.width - 1).count { x ->
                    val color = pixels[x, y]
                    color.red > 0.98f && color.green > 0.98f && color.blue > 0.98f
                }
            assertTrue("Step ${step + 1} must not leave a white gap between the fill and track: $whiteGap px", whiteGap <= 1)
            if (step < 2) compose.onNodeWithTag("signup-next").performClick()
        }
    }
}
