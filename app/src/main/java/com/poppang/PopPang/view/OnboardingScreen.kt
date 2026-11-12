package com.poppang.PopPang.view

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poppang.PopPang.R
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.view.renewal.RenewalIcon
import com.poppang.PopPang.view.renewal.RenewalNotoMedium
import com.poppang.PopPang.view.renewal.RenewalPrimaryButton
import com.poppang.PopPang.view.renewal.RenewalRegular
import com.poppang.PopPang.view.renewal.RenewalSemiBold
import com.poppang.PopPang.view.renewal.renewalText
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(onStartClick: () -> Unit = {}) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var finishing by remember { mutableStateOf(false) }
    val finish: () -> Unit = {
        if (!finishing) {
            finishing = true
            onStartClick()
        }
    }
    val titles =
        listOf(
            "놓치기 싫은 팝업,\n키워드만 등록하세요",
            "지금 근처에서 열린 팝업,\n지도로 한눈에",
            "가고 싶은 팝업은\n캘린더에 찜해두세요",
        )
    val descriptions =
        listOf(
            "브랜드, 캐릭터, 지역 키워드를 등록하면\n관련 팝업이 열리는 순간 알려드려요.",
            "지도 앱엔 안 나오는 팝업까지,\n거리와 마감 시간을 함께 보여드려요.",
            "캘린더에 찜한 팝업을\n한눈에 정리해드려요.",
        )
    val icons = listOf(R.drawable.renewal_asset_33ffd, R.drawable.renewal_asset_eecbd, R.drawable.renewal_asset_8eb01)
    BackHandler(page > 0) { if (!finishing) page-- }
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 393.dp)
            .fillMaxWidth()
            .systemBarsPadding()
            .testTag("onboarding-screen"),
    ) {
        Box(Modifier.fillMaxWidth().height(37.dp).padding(horizontal = 20.dp)) {
            androidx.compose.animation.AnimatedVisibility(
                visible = page < titles.lastIndex,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(120)),
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Text(
                    "건너뛰기",
                    style = renewalText(14).copy(lineHeight = 22.sp),
                    color = Color(0xFF8B8B8B),
                    modifier = Modifier.clickable(enabled = page < titles.lastIndex && !finishing, onClick = finish),
                )
            }
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(
                targetState = page,
                modifier = Modifier.fillMaxSize().clipToBounds().testTag("onboarding-content"),
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    val enter = slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 5 * direction } + fadeIn(tween(220))
                    val exit = slideOutHorizontally(tween(200, easing = FastOutSlowInEasing)) { -it / 5 * direction } + fadeOut(tween(160))
                    (enter togetherWith exit).using(null)
                },
                label = "onboarding-page",
            ) { displayedPage ->
                val baseHeight = if (displayedPage == 0) 379f else 383f
                val artHeight = minOf((maxWidth - 40.dp) * (baseHeight / 353f), (maxHeight - 200.dp).coerceAtLeast(140.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    OnboardingArt(
                        displayedPage,
                        Modifier.align(Alignment.CenterHorizontally).height(artHeight).aspectRatio(
                            353f / baseHeight,
                        ),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        if (displayedPage == 1) {
                            Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                                Image(painterResource(icons[displayedPage]), null, Modifier.size(22.dp, 26.9997.dp))
                            }
                        } else {
                            RenewalIcon(icons[displayedPage], size = 30.dp)
                        }
                        Text(
                            titles[displayedPage],
                            style = renewalText(24, true, 1.47f).copy(color = Color.Black),
                            modifier = Modifier.testTag("onboarding-title"),
                        )
                        Text(
                            descriptions[displayedPage],
                            style = renewalText(14, lineHeight = 1.47f).copy(fontFamily = RenewalRegular),
                            color = Color(0xFF555555),
                        )
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().height(18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val dotWidth by animateDpAsState(if (index == page) 17.dp else 8.dp, tween(220), label = "onboarding-dot-width-$index")
                val dotColor by animateColorAsState(
                    if (index == page) Color(0xFF333333) else Color(0xFFD9D9D9),
                    tween(220),
                    label = "onboarding-dot-color-$index",
                )
                Box(
                    Modifier
                        .size(dotWidth, 8.dp)
                        .background(dotColor, RoundedCornerShape(50)),
                )
            }
        }
        Spacer(Modifier.height(25.dp))
        RenewalPrimaryButton(
            if (page == 2) "시작하기" else "다음",
            {
                if (!finishing) {
                    if (page < titles.lastIndex) page++ else finish()
                }
            },
            Modifier.padding(start = 23.dp, end = 23.dp, bottom = 16.dp).testTag("onboarding-next"),
            arrow = true,
        )
    }
}

@Composable
private fun OnboardingArt(
    page: Int,
    modifier: Modifier,
) {
    val designHeight = if (page == 0) 379.dp else 383.dp
    Layout(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        content = {
            Box(Modifier.size(353.dp, designHeight).background(Color(0xFFFEF2E7))) {
                when (page) {
                    0 ->
                        Column(
                            Modifier.fillMaxSize().padding(horizontal = 11.dp, vertical = 30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(
                                Modifier.width(321.dp).height(37.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                listOf("# 성수", "# 짱구", "# 두쫀쿠").forEachIndexed { index, label ->
                                    Surface(
                                        color = if (index < 2) mainOrange else Color.White,
                                        shape = RoundedCornerShape(18.dp),
                                        shadowElevation = 1.dp,
                                    ) {
                                        Text(
                                            label,
                                            style =
                                                renewalText(14).copy(
                                                    fontFamily = RenewalNotoMedium,
                                                    lineHeight = 16.8.sp,
                                                    letterSpacing = (-0.28).sp,
                                                ),
                                            color = if (index < 2) Color.White else Color(0xFF333333),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(30.dp))
                            NotificationExample(Modifier.width(304.243.dp))
                            Spacer(Modifier.height(17.dp))
                            NotificationExample(Modifier.width(272.dp), alpha = .84f, secondary = true)
                            Spacer(Modifier.height(17.dp))
                            NotificationExample(Modifier.width(258.dp), alpha = .6f, compact = true)
                        }
                    1 -> CroppedOnboardingImage(R.drawable.renewal_asset_ae67c, 1.002f, 1.9958f, -.001f, -.3474f, Modifier.fillMaxSize())
                    else -> {
                        CroppedOnboardingImage(
                            R.drawable.renewal_asset_3642a,
                            1.0022f,
                            2.0952f,
                            -.0011f,
                            -.3691f,
                            Modifier.offset(x = 34.dp, y = 21.dp).size(283.dp, 295.dp).clip(RoundedCornerShape(20.dp)),
                        )
                        NotificationExample(
                            Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).width(304.243.dp),
                            shadowAlpha = .08f,
                        )
                    }
                }
            }
        },
    ) { children, constraints ->
        val designWidthPx = 353.dp.roundToPx()
        val child = children.single().measure(Constraints.fixed(designWidthPx, designHeight.roundToPx()))
        val scale = constraints.maxWidth.toFloat() / designWidthPx
        layout(constraints.maxWidth, constraints.maxHeight) {
            child.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

@Composable
private fun NotificationExample(
    modifier: Modifier,
    alpha: Float = 1f,
    compact: Boolean = false,
    secondary: Boolean = false,
    shadowAlpha: Float = .25f,
) {
    val shape = RoundedCornerShape(10.dp)
    Surface(
        modifier
            .dropShadow(
                shape,
                Shadow(
                    radius = 15.dp,
                    offset = DpOffset(0.dp, 2.dp),
                    color =
                        Color.Black.copy(
                            alpha =
                                shadowAlpha * alpha,
                        ),
                ),
            ).graphicsLayer {
                this.alpha = alpha
                compositingStrategy = CompositingStrategy.Offscreen
            },
        color = Color.White.copy(alpha = if (compact) 1f else .96f),
        shape = shape,
    ) {
        Row(
            Modifier.height(if (compact) 56.dp else 74.dp).padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            val logoSize = if (compact) 32.dp else 38.dp
            Box(Modifier.size(logoSize).background(Color(0xFFDE5C00), RoundedCornerShape(10.dp))) {
                Image(
                    painterResource(if (compact) R.drawable.renewal_asset_8d445 else R.drawable.renewal_asset_48880),
                    null,
                    Modifier
                        .offset(x = if (compact) 10.5952.dp else 12.5818.dp, y = if (compact) 5.7824.dp else 6.8666.dp)
                        .size(if (compact) 10.7812.dp else 12.8027.dp, if (compact) 14.375.dp else 17.0703.dp),
                )
                Image(
                    painterResource(if (compact) R.drawable.renewal_asset_111e5 else R.drawable.renewal_asset_37051),
                    null,
                    Modifier
                        .offset(x = if (compact) 5.344.dp else 6.3422.dp, y = if (compact) 15.0144.dp else 17.8296.dp)
                        .size(if (compact) 21.25.dp else 25.2345.dp, if (compact) 13.4688.dp else 15.9941.dp),
                )
            }
            Column(
                Modifier.weight(1f).then(if (compact) Modifier else Modifier.height(54.dp)),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().then(if (compact) Modifier else Modifier.weight(1f)).padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "POP PANG",
                        style = renewalText(if (compact) 11 else 12).copy(fontFamily = RenewalSemiBold),
                        color = Color(0xFF333333),
                    )
                    Text("방금", style = renewalText(10).copy(fontFamily = RenewalRegular, lineHeight = 16.sp), color = Color(0xFFADADAD))
                }
                Text(
                    if (compact) "#화장품 팝업이 부산에 새로 등록 됐어요" else "#화장품 팝업이 부산에서 열렸어요!\n지금 바로 확인해 보세요.",
                    style =
                        renewalText(
                            if (compact) {
                                10
                            } else if (secondary) {
                                11
                            } else {
                                12
                            },
                        ).copy(fontFamily = RenewalRegular, lineHeight = if (compact) 12.sp else 16.sp),
                    color = Color(0xFF444444),
                    maxLines = if (compact) 1 else 2,
                )
            }
        }
    }
}

@Composable
private fun CroppedOnboardingImage(
    resource: Int,
    widthScale: Float,
    heightScale: Float,
    offsetX: Float,
    offsetY: Float,
    modifier: Modifier,
) {
    Layout(content = {
        Image(painterResource(resource), null, contentScale = ContentScale.FillBounds)
    }, modifier = modifier.clipToBounds()) { children, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val child = children.single().measure(Constraints.fixed((width * widthScale).roundToInt(), (height * heightScale).roundToInt()))
        layout(width, height) { child.place((width * offsetX).roundToInt(), (height * offsetY).roundToInt()) }
    }
}
