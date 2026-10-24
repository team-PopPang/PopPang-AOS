package com.poppang.PopPang.view

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.poppang.PopPang.ui.theme.mainBlack
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun rememberMapSheetState(
    initialValue: MapSheetValue = MapSheetValue.Middle
): AnchoredDraggableState<MapSheetValue> = rememberSaveable(
    saver = AnchoredDraggableState.Saver()
) {
    AnchoredDraggableState(initialValue)
}

@Composable
internal fun MapSheetScaffold(
    sheetState: AnchoredDraggableState<MapSheetValue>,
    modifier: Modifier = Modifier,
    middleHeightFraction: Float = 0.45f,
    expandedHeightFraction: Float = 0.85f,
    sheetHeader: (@Composable ColumnScope.() -> Unit)? = null,
    sheetContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier.fillMaxSize()) {
        content()
        Layout(
            modifier = Modifier.fillMaxSize().clipToBounds(),
            content = {
                Surface(
                    modifier = Modifier
                        .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                        .fillMaxWidth(),
                    shape = BottomSheetDefaults.ExpandedShape,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    tonalElevation = 8.dp
                ) {
                    Column {
                        MapSheetHeader(sheetState, sheetHeader)
                        sheetContent()
                    }
                }
            }
        ) { measurables, constraints ->
            val layoutHeight = constraints.maxHeight
            sheetState.updateAnchors(
                DraggableAnchors {
                    MapSheetValue.Hidden at layoutHeight.toFloat()
                    MapSheetValue.Middle at layoutHeight * (1f - middleHeightFraction)
                    MapSheetValue.Expanded at layoutHeight * (1f - expandedHeightFraction)
                },
                newTarget = sheetState.targetValue
            )
            val sheetTop = sheetState.requireOffset().roundToInt()
            val visibleHeight = (layoutHeight - sheetTop).coerceIn(0, layoutHeight)
            // Measure against the visible height so the last row is reachable at either height.
            val sheet = measurables.single().measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = visibleHeight,
                    maxHeight = visibleHeight
                )
            )
            layout(constraints.maxWidth, layoutHeight) {
                sheet.placeRelative((constraints.maxWidth - sheet.width) / 2, sheetTop)
            }
        }
    }
}

@Composable
private fun MapSheetHeader(
    sheetState: AnchoredDraggableState<MapSheetValue>,
    content: (@Composable ColumnScope.() -> Unit)?
) {
    val scope = rememberCoroutineScope()
    val dragThreshold = with(LocalDensity.current) { 24.dp.toPx() }
    val animationSpec = tween<Float>(durationMillis = 240)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // The handle and sorting row share the drag area; list scrolls stay independent.
            .anchoredDraggable(
                state = sheetState,
                orientation = Orientation.Vertical,
                flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                    state = sheetState,
                    positionalThreshold = { minOf(it * 0.15f, dragThreshold) },
                    animationSpec = animationSpec
                )
            )
            .semantics {
                contentDescription = "지도 패널 높이 조절"
                stateDescription = when (sheetState.settledValue) {
                    MapSheetValue.Hidden -> "숨김"
                    MapSheetValue.Middle -> "중간"
                    MapSheetValue.Expanded -> "펼침"
                }
                customActions = listOf(
                    CustomAccessibilityAction("펼치기") {
                        scope.launch { sheetState.animateTo(MapSheetValue.Expanded, animationSpec) }
                        true
                    },
                    CustomAccessibilityAction("접기") {
                        val target = if (sheetState.settledValue == MapSheetValue.Expanded) {
                            MapSheetValue.Middle
                        } else {
                            MapSheetValue.Hidden
                        }
                        scope.launch { sheetState.animateTo(target, animationSpec) }
                        true
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(if (content == null) 48.dp else 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(mainBlack, RoundedCornerShape(2.dp))
            )
        }
        content?.invoke(this)
    }
}
