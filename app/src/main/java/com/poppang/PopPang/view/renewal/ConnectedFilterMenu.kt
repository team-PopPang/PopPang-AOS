package com.poppang.PopPang.view.renewal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/** Options join the original chip from below without replacing or moving its header. */
@Composable
internal fun ConnectedFilterMenu(
    label: String,
    visibility: MutableTransitionState<Boolean>,
    anchorWidth: Dp,
    maxHeight: Dp,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Retain the popup until its closing transition finishes.
    if (!visibility.currentState && !visibility.targetState && visibility.isIdle) return
    val density = LocalDensity.current
    val windowWidth =
        with(density) {
            LocalWindowInfo.current.containerSize.width
                .toDp()
        }
    val width = maxOf(220.dp, anchorWidth).coerceAtMost((windowWidth - 32.dp).coerceAtLeast(0.dp))
    val margin = with(density) { 16.dp.roundToPx() }
    val overlap = with(density) { 1.dp.roundToPx() }
    val widthPx = with(density) { width.roundToPx() }
    var connection by remember { mutableStateOf(IntRange.EMPTY) }
    val position =
        remember(margin, overlap) {
            ConnectedFilterMenuPosition(margin, overlap) { connection = it }
        }
    Popup(
        popupPositionProvider = position,
        onDismissRequest = { if (visibility.targetState) onDismiss() },
        properties = PopupProperties(focusable = visibility.targetState),
    ) {
        AnimatedVisibility(
            visibleState = visibility,
            enter = expandVertically(tween(220, easing = FastOutSlowInEasing), expandFrom = Alignment.Top) + fadeIn(tween(150)),
            exit = shrinkVertically(tween(180, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top) + fadeOut(tween(130)),
            modifier = Modifier.testTag("map-filter-motion-$label"),
        ) {
            Surface(
                modifier =
                    Modifier
                        .width(width)
                        .heightIn(max = maxHeight)
                        .testTag("map-filter-menu-$label")
                        .semantics { paneTitle = "$label 선택" }
                        .drawWithContent {
                            drawContent()
                            // Leave one continuous white connection across the chip's bottom border.
                            if (!connection.isEmpty()) {
                                val stroke = 1.dp.toPx()
                                val start = connection.first.toFloat().coerceAtLeast(stroke)
                                val end = (connection.last + 1).toFloat().coerceAtMost(size.width - stroke)
                                if (end > start) {
                                    drawRect(Color.White, Offset(start, 0f), Size(end - start, overlap.toFloat()))
                                }
                            }
                        },
                shape =
                    RoundedCornerShape(
                        topStart = if (!connection.isEmpty() && connection.first <= 0) 0.dp else 14.dp,
                        topEnd = if (!connection.isEmpty() && connection.last >= widthPx - 1) 0.dp else 14.dp,
                        bottomStart = 14.dp,
                        bottomEnd = 14.dp,
                    ),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
            ) {
                Column(
                    Modifier
                        .then(if (!visibility.targetState) Modifier.clearAndSetSemantics {} else Modifier)
                        .pointerInput(visibility.targetState) {
                            if (!visibility.targetState) {
                                awaitPointerEventScope {
                                    while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                                }
                            }
                        }.then(if (label == "카테고리") Modifier else Modifier.verticalScroll(rememberScrollState()))
                        .padding(vertical = 8.dp),
                    content = content,
                )
            }
        }
    }
}

internal class ConnectedFilterMenuPosition(
    private val edgeMargin: Int,
    private val overlap: Int = 1,
    private val onConnection: (IntRange) -> Unit = {},
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val preferredX = if (layoutDirection == LayoutDirection.Ltr) anchorBounds.left else anchorBounds.right - popupContentSize.width
        val preferredY = anchorBounds.bottom - overlap
        val result =
            IntOffset(
                fit(preferredX, windowSize.width - popupContentSize.width),
                fit(preferredY, windowSize.height - popupContentSize.height),
            )
        onConnection(
            if (result.y == preferredY) {
                (anchorBounds.left - result.x).coerceAtLeast(0) until
                    (anchorBounds.right - result.x).coerceAtMost(popupContentSize.width)
            } else {
                IntRange.EMPTY
            },
        )
        return result
    }

    private fun fit(
        preferred: Int,
        remaining: Int,
    ): Int {
        val start = minOf(edgeMargin, remaining.coerceAtLeast(0))
        val end = (remaining - edgeMargin).coerceAtLeast(start)
        return preferred.coerceIn(start, end)
    }
}
