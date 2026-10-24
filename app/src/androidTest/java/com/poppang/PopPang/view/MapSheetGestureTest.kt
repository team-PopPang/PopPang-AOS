package com.poppang.PopPang.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MapSheetGestureTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var sheetState: AnchoredDraggableState<MapSheetValue>
    private lateinit var listState: LazyListState
    private var mapTapCount = 0

    @Test
    fun middleSheetScrollsDownWithoutChangingHeight() {
        showSheet(MapSheetValue.Middle, firstVisibleItem = 10)

        swipeList(downward = true)

        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Middle, sheetState.settledValue)
            assertTrue(listState.firstVisibleItemIndex < 10)
        }
    }

    @Test
    fun expandedSheetStaysExpandedWhenListIsDraggedPastItsTop() {
        showSheet(MapSheetValue.Expanded)

        swipeList(downward = true)

        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Expanded, sheetState.settledValue)
            assertEquals(0, listState.firstVisibleItemIndex)
        }
    }

    @Test
    fun middleSheetScrollsUpWithoutChangingHeight() {
        showSheet(MapSheetValue.Middle)

        swipeList(downward = false)

        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Middle, sheetState.settledValue)
            assertTrue(
                "Expected the list to scroll within the middle-height sheet",
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
            )
        }
    }

    @Test
    fun expandedSheetStillScrollsItsList() {
        showSheet(MapSheetValue.Expanded, firstVisibleItem = 10)

        swipeList(downward = true)

        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Expanded, sheetState.settledValue)
            assertTrue(listState.firstVisibleItemIndex < 10)
        }
    }

    @Test
    fun middleSheetStaysAtMiddleWhenListIsDraggedPastItsTop() {
        showSheet(MapSheetValue.Middle)

        swipeList(downward = true)

        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Middle, sheetState.settledValue)
            assertEquals(0, listState.firstVisibleItemIndex)
        }
    }

    @Test
    fun lastListItemIsReachableAtMiddleHeight() {
        showSheet(MapSheetValue.Middle)

        composeRule.onNodeWithTag("map-sheet-list").performScrollToIndex(39)

        composeRule.onNodeWithText("Popup 39").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Middle, sheetState.settledValue)
            assertFalse(listState.canScrollForward)
        }
    }

    @Test
    fun broadHeaderExpandsMiddleSheetWithOneUpwardSwipe() {
        showSheet(MapSheetValue.Middle, firstVisibleItem = 10)

        swipeHeader(downward = false)

        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Expanded, sheetState.settledValue)
            assertEquals(10, listState.firstVisibleItemIndex)
        }
    }

    @Test
    fun broadHeaderMovesThroughAllThreeHeightsWithOneSwipePerStep() {
        showSheet(MapSheetValue.Expanded, firstVisibleItem = 10)

        swipeHeader(downward = true)
        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Middle, sheetState.settledValue)
            assertEquals(10, listState.firstVisibleItemIndex)
        }

        swipeHeader(downward = true)
        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Hidden, sheetState.settledValue)
            assertEquals(10, listState.firstVisibleItemIndex)
        }
    }

    @Test
    fun sortingRowBackgroundExpandsSheetWithoutScrollingList() {
        showSheet(MapSheetValue.Middle, firstVisibleItem = 10, withSortHeader = true)

        composeRule.onNodeWithTag("map-sheet-sort-row").performTouchInput {
            val start = Offset(width * 0.15f, centerY)
            swipe(start, start - Offset(0f, 200f), durationMillis = 350)
        }

        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Expanded, sheetState.settledValue)
            assertEquals(10, listState.firstVisibleItemIndex)
        }
    }

    @Test
    fun draggingSortButtonDownHidesSheetWithoutOpeningSortMenu() {
        showSheet(MapSheetValue.Middle, firstVisibleItem = 10, withSortHeader = true)

        composeRule.onNodeWithText("가까운순").performTouchInput {
            swipe(center, center + Offset(0f, 200f), durationMillis = 350)
        }

        composeRule.runOnIdle {
            assertEquals(MapSheetValue.Hidden, sheetState.settledValue)
            assertEquals(10, listState.firstVisibleItemIndex)
        }
        composeRule.onNodeWithText("정렬").assertDoesNotExist()
    }

    @Test
    fun tappingSortButtonStillAllowsSelectingSortOrder() {
        showSheet(MapSheetValue.Middle, withSortHeader = true)

        composeRule.onNodeWithText("가까운순").performTouchInput { click() }
        composeRule.onNodeWithText("최신순").performClick()

        composeRule.onNodeWithText("최신순").assertIsDisplayed()
        composeRule.onNodeWithText("정렬").assertDoesNotExist()
        composeRule.runOnIdle { assertEquals(MapSheetValue.Middle, sheetState.settledValue) }
    }

    @Test
    fun mapAboveSheetStillReceivesTouches() {
        showSheet(MapSheetValue.Middle)

        composeRule.onNodeWithTag("map-background").performTouchInput {
            click(Offset(centerX, 30f))
        }

        composeRule.runOnIdle { assertEquals(1, mapTapCount) }
    }

    @Test
    fun shortDetailSheetKeepsItsFullHeightAndBlocksMapTouchesBelowItsText() {
        composeRule.setContent {
            sheetState = rememberMapSheetState()
            MapSheetScaffold(
                sheetState = sheetState,
                middleHeightFraction = 0.60f,
                expandedHeightFraction = 0.90f,
                sheetContent = { Text("Short detail") }
            ) {
                Box(
                    Modifier.fillMaxSize().testTag("map-background")
                        .clickable { mapTapCount++ }
                )
            }
        }

        composeRule.onNodeWithTag("map-background").performTouchInput {
            click(Offset(centerX, height * 0.9f))
        }

        composeRule.runOnIdle { assertEquals(0, mapTapCount) }
    }

    private fun showSheet(
        initialValue: MapSheetValue,
        firstVisibleItem: Int = 0,
        withSortHeader: Boolean = false
    ) {
        composeRule.setContent {
            sheetState = rememberMapSheetState(initialValue)
            listState = rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleItem)
            var selectedSort by remember { mutableStateOf("CLOSEST") }
            MapSheetScaffold(
                sheetState = sheetState,
                sheetHeader = if (withSortHeader) {
                    {
                        MapSheetSortHeader(
                            selectedSort = selectedSort,
                            onSortSelected = { selectedSort = it },
                            modifier = Modifier.testTag("map-sheet-sort-row")
                        )
                    }
                } else null,
                sheetContent = {
                    MapSheetList(
                        modifier = Modifier.testTag("map-sheet-list"),
                        state = listState
                    ) {
                        items(40) { index ->
                            Text("Popup $index", Modifier.fillMaxWidth().height(80.dp))
                        }
                    }
                }
            ) {
                Box(
                    Modifier.fillMaxSize().testTag("map-background")
                        .clickable { mapTapCount++ }
                )
            }
        }
    }

    private fun swipeList(downward: Boolean) {
        composeRule.onNodeWithTag("map-sheet-list").performTouchInput {
            val upper = Offset(centerX, 30f)
            val lower = Offset(centerX, 230f)
            swipe(
                start = if (downward) upper else lower,
                end = if (downward) lower else upper,
                durationMillis = 350
            )
        }
        composeRule.waitForIdle()
    }

    private fun swipeHeader(downward: Boolean) {
        composeRule.onNodeWithContentDescription("지도 패널 높이 조절").performTouchInput {
            // Start well away from the small center bar to check the whole header touch area.
            val start = Offset(width * 0.15f, centerY)
            swipe(
                start = start,
                end = start + Offset(0f, if (downward) 200f else -200f),
                durationMillis = 350
            )
        }
        composeRule.waitForIdle()
    }
}
