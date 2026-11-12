package com.poppang.PopPang.view.renewal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RenewalTextBoundsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun everyBottomTabKeepsItsFullLabelAtNormalAndLargeFontSizes() {
        val selected = mutableIntStateOf(0)
        val fontScale = mutableStateOf(1f)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale.value)) {
                PopPangAOSTheme(false) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        RenewalBottomBar(selected.intValue) { selected.intValue = it }
                    }
                }
            }
        }
        val labels = listOf("홈", "캘린더", "팝팡지도", "팝팡", "마이")
        for (scale in listOf(1f, 2f)) {
            compose.runOnIdle { fontScale.value = scale }
            labels.forEachIndexed { index, label ->
                compose.onNodeWithText(label).performClick().assertIsSelected()
                compose.runOnIdle { assertEquals(index, selected.intValue) }
                labels.forEach { visible ->
                    assertFullText(compose.onNodeWithText(visible, useUnmergedTree = true), "$visible at font scale $scale")
                }
            }
        }
    }

    @Test fun collapsedMapHeaderKeepsTheSortLabelVisibleBeforeAndAfterSelection() {
        val fontScale = mutableStateOf(1f)
        var requestedSort = ""
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale.value)) {
                PopPangAOSTheme(false) {
                    RenewalMapUi(
                        emptyList(),
                        emptySet(),
                        {},
                        {},
                        onSortChanged = { requestedSort = it },
                        mapContent = { _, _, _ -> Box(Modifier.fillMaxSize()) },
                    )
                }
            }
        }
        assertFullText(compose.onNodeWithText("가까운순", useUnmergedTree = true), "Initial sort")
        compose.onNodeWithText("가까운순").performClick()
        compose.onNodeWithText("최신순").performClick()
        compose.runOnIdle { assertEquals("NEWEST", requestedSort) }
        assertFullText(compose.onNodeWithText("최신순", useUnmergedTree = true), "Changed sort")
        compose.runOnIdle { fontScale.value = 2f }
        assertFullText(compose.onNodeWithText("최신순", useUnmergedTree = true), "Large sort")
        assertFullText(compose.onNodeWithText("주변 팝업", useUnmergedTree = true), "Large heading")
    }

    private fun assertFullText(
        node: SemanticsNodeInteraction,
        description: String,
    ) {
        val layouts = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> action(layouts) }
        assertTrue("$description must expose a text layout", layouts.isNotEmpty())
        layouts.forEach { layout ->
            assertFalse("$description must not lose text vertically", layout.didOverflowHeight)
            repeat(layout.lineCount) { line ->
                assertFalse("$description must not replace characters with an ellipsis", layout.isLineEllipsized(line))
                assertTrue(
                    "$description must keep the glyphs within the measured width",
                    layout.getLineLeft(line) >= -1f && layout.getLineRight(line) <= layout.size.width + 1f,
                )
            }
        }
    }
}
