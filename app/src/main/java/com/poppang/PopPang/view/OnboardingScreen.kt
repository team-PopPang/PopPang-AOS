package com.poppang.PopPang.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.poppang.PopPang.R


@Composable
fun OnboardingScreen(
    onStartClick: () -> Unit = {}
) {
    val images = listOf(
        R.drawable.onbording_1,
        R.drawable.onbording_2,
        R.drawable.onbording_3,

    )
    val buttonLabels = listOf("다음", "다음", "시작하기  →")

    var page by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF)),
    ) {
        TextButton(
            onClick = { onStartClick() },
            modifier = Modifier
                .align(TopEnd)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    end = 16.dp
                )
        ) {
            Text(text = "건너뛰기", color = Color.Black)
        }
        Image(
            painter = painterResource(id = images[page]),
            contentDescription = null,
            modifier = Modifier
                .align(TopCenter)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())

        )
        ImageIndicator(
            modifier = Modifier
                .align(BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 50.dp),
            currentPage = { page },
            totalPages = { images.size }
        )
        CustomButton(
            onClick = {
                if (page < 2) {
                    page++
                } else {
                    onStartClick()
                }
            },
            text = buttonLabels[page],
            modifier = Modifier
                .align(BottomCenter)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 20.dp)
        )
    }
}

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


@Composable
@Preview
fun OnboardingScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFC300))
    )
    OnboardingScreen()
}