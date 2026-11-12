package com.poppang.PopPang.view.renewal

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.test.espresso.Espresso
import com.poppang.PopPang.R
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MapControlsLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun theCombinedSheetOnlyOffersCategoriesAndPeriodsAndAppliesBothTogether() {
        var applied: RenewalMapFilters? = null
        compose.setContent {
            PopPangAOSTheme(false) {
                RenewalFilterSheet(
                    initial = RenewalMapFilters(),
                    categories = listOf("디저트", "캐릭터"),
                    popups = emptyList(),
                    query = "",
                    onDismiss = {},
                    onApply = { applied = it },
                )
            }
        }
        compose.onNodeWithText("상태").assertDoesNotExist()
        compose.onNodeWithText("진행 중").assertDoesNotExist()
        compose.onNodeWithText("오픈 예정").assertDoesNotExist()
        compose.onNodeWithText("기간").assertIsDisplayed()
        compose.onNodeWithText("카테고리").assertIsDisplayed()
        compose.onNodeWithText("오늘").performClick()
        compose.onNodeWithText("캐릭터").performClick()
        compose.runOnIdle { assertEquals(null, applied) }
        compose.onNodeWithTag("map-filter-apply").performClick()
        compose.runOnIdle {
            assertEquals(PopupPeriod.Today, applied?.period)
            assertEquals(setOf("캐릭터"), applied?.categories)
            assertEquals(2, applied?.activeCount)
        }
    }

    @Test fun openingMenusKeepsAllChipBoundsAndLabelsInPlace() {
        var changes = 0
        compose.setContent {
            PopPangAOSTheme(false) {
                FilterShortcuts(RenewalMapFilters(), listOf("디저트", "캐릭터"), { changes++ }, {})
            }
        }
        compose.onNodeWithText("상태").assertDoesNotExist()
        compose.onNodeWithTag("map-filter-chip-상태").assertDoesNotExist()
        val chips = listOf("map-filter-chip-카테고리", "map-period-chip")
        val originalBounds = chips.associateWith { compose.onNodeWithTag(it).getUnclippedBoundsInRoot() }
        for (label in listOf("카테고리", "기간")) {
            compose.onNodeWithText(label).performClick()
            val menu = "map-filter-menu-$label"
            compose.onNodeWithTag(menu).assertIsDisplayed()
            chips.forEach { tag ->
                assertEquals(
                    "$tag must stay in place when $label opens",
                    originalBounds[tag],
                    compose.onNodeWithTag(tag).getUnclippedBoundsInRoot(),
                )
            }
            val option =
                when (label) {
                    "카테고리" -> "디저트"
                    else -> "직접 선택"
                }
            compose.onNodeWithText(option).assertIsDisplayed().assert(hasAnyAncestor(hasTestTag(menu)))
            compose.onNodeWithText(label).assertIsDisplayed()
            Espresso.pressBack()
            compose.onNodeWithTag(menu).assertDoesNotExist()
        }
        compose.runOnIdle { assertEquals(0, changes) }
    }

    @Test fun mapToggleLabelsStayCenteredAndUnclipped() {
        var scale by mutableStateOf(1f)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                PopPangAOSTheme(false) {
                    Column {
                        MapToggleButton("목록 보기", R.drawable.renewal_asset_72354) {}
                        MapToggleButton("지도 보기", R.drawable.renewal_asset_84cce, dark = true) {}
                    }
                }
            }
        }
        for (fontScale in listOf(1f, 2f)) {
            compose.runOnIdle { scale = fontScale }
            for (label in listOf("목록 보기", "지도 보기")) {
                val button = compose.onNodeWithTag("map-toggle-$label").getUnclippedBoundsInRoot()
                val icon = compose.onNodeWithTag("map-toggle-icon-$label", useUnmergedTree = true).getUnclippedBoundsInRoot()
                val text = compose.onNodeWithText(label, useUnmergedTree = true)
                val textBounds = text.getUnclippedBoundsInRoot()
                val buttonCenter = (button.top.value + button.bottom.value) / 2f
                assertEquals("$label icon must be centered", buttonCenter, (icon.top.value + icon.bottom.value) / 2f, .5f)
                assertEquals("$label text must be centered", buttonCenter, (textBounds.top.value + textBounds.bottom.value) / 2f, .5f)
                val layouts = mutableListOf<TextLayoutResult>()
                text.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                assertTrue(layouts.isNotEmpty())
                layouts.forEach {
                    assertFalse("$label must not clip vertically at $fontScale", it.didOverflowHeight)
                    assertTrue("$label glyphs must fit at $fontScale", it.getLineRight(0) <= it.size.width + 1f)
                }
            }
        }
    }

    @Test fun categoryActionsStayFixedWhileOnlyTheChoicesScroll() {
        var filters by mutableStateOf(RenewalMapFilters())
        var changes = 0
        val categories = (1..30).map { "카테고리 $it" }
        compose.setContent {
            PopPangAOSTheme(false) {
                FilterShortcuts(filters, categories, {
                    filters = it
                    changes++
                }, {})
            }
        }
        compose.onNodeWithText("카테고리").performClick()
        compose.onNodeWithText("초기화").assertIsDisplayed()
        compose.onNodeWithText("0개 적용").assertIsDisplayed()
        val footer = compose.onNodeWithTag("map-category-actions").getUnclippedBoundsInRoot()
        val listBefore = compose.onNodeWithTag("map-category-list").getUnclippedBoundsInRoot()
        assertTrue("The list must end above the fixed actions", listBefore.bottom <= footer.top)
        compose.onNodeWithText(categories.first()).performClick()
        compose.onNodeWithText("1개 적용").assertIsDisplayed()
        compose.onNodeWithText("초기화").performClick()
        compose.onNodeWithText("0개 적용").assertIsDisplayed()
        compose
            .onNodeWithText(categories.last())
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        assertEquals(footer, compose.onNodeWithTag("map-category-actions").getUnclippedBoundsInRoot())
        compose.onNodeWithText("초기화").assertIsDisplayed()
        compose.onNodeWithText("1개 적용").assertIsDisplayed()
        compose.runOnIdle { assertEquals("Draft changes must wait for Apply", 0, changes) }
        compose.onNodeWithText("1개 적용").performClick()
        compose.runOnIdle {
            assertEquals(setOf(categories.last()), filters.categories)
            assertEquals(1, changes)
        }
    }
}
