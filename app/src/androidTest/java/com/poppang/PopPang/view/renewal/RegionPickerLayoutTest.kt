package com.poppang.PopPang.view.renewal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import com.poppang.PopPang.model.RegionsResponse
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import com.poppang.PopPang.view.CalendarLocalFilterButton
import com.poppang.PopPang.view.FilterSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RegionPickerLayoutTest {
    @get:Rule val compose = createComposeRule()

    private val regions =
        (1..25).map { RegionsResponse("지역$it", listOf("전체")) } +
            RegionsResponse("서울", listOf("전체") + (1..40).map { "구$it" })

    @Test fun thePanelUsesHalfHeightAndBothListsScrollBelowAFixedHeader() {
        var scale by mutableStateOf(1f)
        var selectedTab = 2
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                PopPangAOSTheme(false) {
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxWidth().height(600.dp).testTag("region-test-viewport")) {
                            RenewalRegionPicker(true, "서울", regions, {}, {}, Modifier.matchParentSize())
                        }
                        Box(Modifier.fillMaxWidth().testTag("region-test-navigation")) {
                            RenewalBottomBar(2) { selectedTab = it }
                        }
                    }
                }
            }
        }
        for (fontScale in listOf(1f, 2f)) {
            compose.runOnIdle { scale = fontScale }
            val viewport = compose.onNodeWithTag("region-test-viewport").getUnclippedBoundsInRoot()
            val panel = compose.onNodeWithTag("map-region-panel").getUnclippedBoundsInRoot()
            val nav = compose.onNodeWithTag("region-test-navigation").getUnclippedBoundsInRoot()
            val header = compose.onNodeWithTag("map-region-header").getUnclippedBoundsInRoot()
            val viewportHeight = viewport.bottom - viewport.top
            val expectedHeight = viewportHeight * .5f
            assertEquals(expectedHeight.value, (panel.bottom - panel.top).value, .5f)
            assertEquals(viewport.bottom.value, panel.bottom.value, .5f)
            assertTrue("The map must remain visible above the region picker", panel.top > viewport.top)
            assertTrue("The region panel must stop before the navigation menu", panel.bottom <= nav.top)
            compose.onNodeWithTag("map-region-provinces").performScrollToNode(hasTestTag("map-region-province-지역25"))
            compose.onNodeWithTag("map-region-province-지역25").assertIsDisplayed()
            compose.onNodeWithTag("map-region-districts").performScrollToNode(hasTestTag("map-region-district-구40"))
            compose.onNodeWithTag("map-region-district-구40").assertIsDisplayed()
            assertEquals(header, compose.onNodeWithTag("map-region-header").getUnclippedBoundsInRoot())
            assertEquals(panel, compose.onNodeWithTag("map-region-panel").getUnclippedBoundsInRoot())
        }
        compose.onNodeWithText("홈").performClick()
        compose.runOnIdle { assertEquals(0, selectedTab) }
    }

    @Test fun theRegionPickerUsesHalfOfTheMapViewport() {
        compose.setContent {
            PopPangAOSTheme(false) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxWidth().testTag("region-test-viewport")) {
                        RenewalMapUi(emptyList(), emptySet(), {}, {}, mapContent = { _, _, _ -> Box(Modifier.fillMaxSize()) })
                        RenewalRegionPicker(true, "서울", regions, {}, {}, Modifier.matchParentSize())
                    }
                    RenewalBottomBar(2) {}
                }
            }
        }
        val viewport = compose.onNodeWithTag("region-test-viewport").getUnclippedBoundsInRoot()
        val region = compose.onNodeWithTag("map-region-panel").getUnclippedBoundsInRoot()
        assertEquals(viewport.bottom.value, region.bottom.value, .5f)
        assertEquals((viewport.bottom - viewport.top).value / 2f, (region.bottom - region.top).value, .5f)
    }

    @Test fun theHomeRegionButtonOpensTheSameHalfHeightPickerAndAppliesADistrict() {
        var visible by mutableStateOf(false)
        var province by mutableStateOf("전체")
        var district by mutableStateOf("전체")
        compose.setContent {
            PopPangAOSTheme(false) {
                Box(Modifier.fillMaxSize().testTag("home-test-viewport")) {
                    FilterSection(
                        selectedRegion = province,
                        selectedDistrict = district,
                        selectedSort = "NEWEST",
                        onRegionClick = { visible = true },
                        onSortChange = {},
                        regionExpanded = visible,
                    )
                    RenewalRegionPicker(visible, province, regions, { visible = false }, {
                        val parts = it.split(" ", limit = 2)
                        province = parts[0]
                        district = parts.getOrNull(1) ?: "전체"
                    }, Modifier.matchParentSize())
                }
            }
        }
        compose.onNodeWithTag("home-region-button").performClick()
        val viewport = compose.onNodeWithTag("home-test-viewport").getUnclippedBoundsInRoot()
        val region = compose.onNodeWithTag("map-region-panel").getUnclippedBoundsInRoot()
        assertEquals((viewport.bottom - viewport.top).value / 2f, (region.bottom - region.top).value, .5f)
        compose.onNodeWithTag("map-region-province-서울").performClick()
        compose.onNodeWithTag("map-region-district-구1").performClick()
        compose.onNodeWithTag("map-region-panel").assertDoesNotExist()
        compose.onNodeWithText("서울 구1").assertIsDisplayed()
    }

    @Test fun theCalendarRegionButtonOpensAHalfHeightPickerAndAppliesADistrict() {
        var visible by mutableStateOf(false)
        var province by mutableStateOf("서울")
        var district = "전체"
        compose.setContent {
            PopPangAOSTheme(false) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxWidth().testTag("calendar-test-viewport")) {
                        CalendarLocalFilterButton(province, { visible = true }, visible)
                        RenewalRegionPicker(visible, province, regions, { visible = false }, {
                            val parts = it.split(" ", limit = 2)
                            province = parts[0]
                            district = parts.getOrNull(1) ?: "전체"
                        }, Modifier.matchParentSize())
                    }
                    RenewalBottomBar(1) {}
                }
            }
        }
        compose.onNodeWithTag("calendar-region-button").performClick()
        val viewport = compose.onNodeWithTag("calendar-test-viewport").getUnclippedBoundsInRoot()
        val region = compose.onNodeWithTag("map-region-panel").getUnclippedBoundsInRoot()
        assertEquals((viewport.bottom - viewport.top).value / 2f, (region.bottom - region.top).value, .5f)
        assertEquals(viewport.bottom.value, region.bottom.value, .5f)
        compose.onNodeWithText("캘린더").assertIsDisplayed()
        compose.onNodeWithTag("map-region-district-구1").performClick()
        compose.onNodeWithTag("map-region-panel").assertDoesNotExist()
        compose.onNodeWithTag("calendar-region-button").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals("서울", province)
            assertEquals("구1", district)
        }
    }

    @Test fun cancelAndBackPreserveTheAppliedRegionAndOnlyADistrictChoiceApplies() {
        var visible by mutableStateOf(true)
        val selections = mutableListOf<String>()
        compose.setContent {
            PopPangAOSTheme(false) {
                Box(Modifier.fillMaxSize()) {
                    RenewalRegionPicker(visible, "서울", regions, { visible = false }, { selections.add(it) }, Modifier.matchParentSize())
                }
            }
        }
        compose.onNodeWithTag("map-region-province-지역1").performClick()
        Espresso.pressBack()
        compose.onNodeWithTag("map-region-panel").assertDoesNotExist()
        compose.runOnIdle {
            assertTrue(selections.isEmpty())
            visible = true
        }
        compose.onNodeWithTag("map-region-province-서울").assertIsSelected()
        compose.onNodeWithTag("map-region-close").performClick()
        compose.onNodeWithTag("map-region-panel").assertDoesNotExist()
        compose.runOnIdle {
            assertTrue(selections.isEmpty())
            visible = true
        }
        compose.onNodeWithTag("map-region-districts").performScrollToNode(hasTestTag("map-region-district-구40"))
        compose.onNodeWithTag("map-region-district-구40").performClick()
        compose.onNodeWithTag("map-region-panel").assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf("서울 구40"), selections) }
    }

    @Test fun regionsCanArriveAfterOpeningAndTheAllOptionRemainsAvailable() {
        var options by mutableStateOf(emptyList<RegionsResponse>())
        val selections = mutableListOf<String>()
        compose.setContent {
            PopPangAOSTheme(false) {
                Box(Modifier.fillMaxSize()) {
                    RenewalRegionPicker(true, "서울", options, {}, { selections.add(it) }, Modifier.matchParentSize())
                }
            }
        }
        compose.onNodeWithTag("map-region-province-전체").assertIsDisplayed()
        compose.runOnIdle { options = regions }
        compose.onNodeWithTag("map-region-province-서울").assertIsSelected()
        compose.onNodeWithTag("map-region-district-구1").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(listOf("서울 구1"), selections) }
        compose.onNodeWithTag("map-region-province-전체").performClick()
        compose.onNodeWithTag("map-region-district-전체").performClick()
        compose.runOnIdle { assertEquals(listOf("서울 구1", "전체"), selections) }
    }
}
