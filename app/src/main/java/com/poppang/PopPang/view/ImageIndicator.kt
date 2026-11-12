package com.poppang.PopPang.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PageDot(
    size: Dp,
    isSelected: Boolean,
    dotSpace: Dp,
) {
    Box(
        modifier = Modifier
            .padding(horizontal = dotSpace / 2)
            .size(size)
            .clip(CircleShape)
            .background(
                color = if (isSelected) Color.Black
                else Color.Black.copy(0.3f)
            )
    )
}

@Composable
fun ImageIndicator(
    modifier: Modifier = Modifier,
    currentPage: () -> Int = { 0 },
    totalPages: () -> Int = { 10 },
    dotSpace: Dp = INDICATOR_DEFAULT_DOT_SPACE,

    ) {
    val dotSize: Dp = dotSpace * 1.5f
    val cursor by remember { derivedStateOf(currentPage) }
    var lineStart by rememberSaveable { mutableIntStateOf(0) }
    var lineEnd by rememberSaveable { mutableIntStateOf(2) }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(cursor) {
        if (cursor !in lineStart..lineEnd) {
            if (cursor + 1 == lineStart) {
                lineStart--
                lineEnd--
            } else {
                lineStart++
                lineEnd++
            }
        }
    }


    Box(modifier = modifier, contentAlignment = Center) {
        LazyRow(
            modifier = modifier,
            verticalAlignment = CenterVertically,
            state = lazyListState,
        ) {
            if (totalPages() > 2) {
                items(totalPages()) { idx ->
                    val animatedSize by animateDpAsState(
                        targetValue = when (idx) {
                            in lineStart..lineEnd -> {
                                dotSize
                            }

                            lineStart - 1, lineEnd + 1 -> {
                                dotSize * 0.6f
                            }

                            lineStart - 2, lineEnd + 2 -> {
                                dotSize * 0.3f
                            }

                            else -> 0.dp
                        }, label = ""
                    )



                    when (idx) {
                        in lineStart..lineEnd -> {
                            // 일반 크기
                            PageDot(
                                size = animatedSize,
                                isSelected = idx == cursor,
                                dotSpace = dotSpace,
                            )
                        }

                        lineStart - 1, lineEnd + 1 -> {
                            // 중간 크기
                            PageDot(
                                size = animatedSize,
                                isSelected = false,
                                dotSpace = dotSpace,
                            )
                        }

                        lineStart - 2, lineEnd + 2 -> {
                            // 작은 크기
                            PageDot(
                                size = animatedSize,
                                isSelected = false,
                                dotSpace = dotSpace,
                            )
                        }

                        else -> {
                            // 자리 차지하지 않도록 Spacer로 처리
                            Spacer(modifier = Modifier.size(animatedSize))
                        }
                    }
                }
            } else {
                items(totalPages()) { idx ->
                    PageDot(
                        size = dotSize,
                        isSelected = idx == cursor,
                        dotSpace = dotSpace,
                    )
                }
            }
        }
    }
}

// 상수 정의
private val INDICATOR_DEFAULT_DOT_SPACE = 4.dp
