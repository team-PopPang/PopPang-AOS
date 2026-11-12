package com.poppang.PopPang.preview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poppang.PopPang.R
import com.poppang.PopPang.ui.theme.PopPangAOSTheme
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.view.OnboardingScreen
import com.poppang.PopPang.view.rememberNotificationOptIn
import com.poppang.PopPang.view.renewal.RenewalBottomBar
import com.poppang.PopPang.view.renewal.RenewalCategories
import com.poppang.PopPang.view.renewal.RenewalIcon
import com.poppang.PopPang.view.renewal.RenewalMapUi
import com.poppang.PopPang.view.renewal.RenewalMarkerRenderer
import com.poppang.PopPang.view.renewal.RenewalPopup
import com.poppang.PopPang.view.renewal.RenewalSignupFlow
import com.poppang.PopPang.view.renewal.renewalText
import com.poppang.PopPang.view.renewal.renewalToday
import kotlin.math.roundToInt

/** Debug-only UI review flow. It never initializes or changes a real login session. */
class RenewalPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialScreen = intent.getStringExtra("screen") ?: "onboarding"
        setContent { PopPangAOSTheme(useDarkTheme = false) { RenewalPreviewApp(initialScreen) } }
    }
}

@Composable
private fun RenewalPreviewApp(initialScreen: String) {
    var screen by rememberSaveable { mutableStateOf(initialScreen) }
    var nickname by rememberSaveable { mutableStateOf("설레는수달") }
    var checked by rememberSaveable { mutableStateOf(false) }
    var categories by rememberSaveable { mutableStateOf(listOf<String>()) }
    var keywords by rememberSaveable { mutableStateOf(listOf<String>()) }
    var completed by rememberSaveable { mutableStateOf(false) }
    // The sample flow requests OS permission but never changes a real account's alert setting.
    val notificationOptIn = rememberNotificationOptIn(onAllowed = { screen = "map" }, onLater = { screen = "map" })
    when (screen) {
        "onboarding" -> OnboardingScreen { screen = "signup" }
        "signup" ->
            RenewalSignupFlow(
                nickname,
                {
                    nickname = it
                    checked = false
                },
                if (checked) "✓ 사용할 수 있는 닉네임이에요" else "",
                checked,
                false,
                { checked = nickname.trim().length >= 2 },
                RenewalCategories,
                categories,
                { categories = if (it in categories) categories - it else categories + it },
                keywords,
                { if (it.isNotBlank() && it !in keywords && keywords.size < 10) keywords = keywords + it.trim() },
                { keywords = keywords - it },
                popularKeywords = listOf("소금빵", "산리오", "짱구", "레고", "향수", "키링"),
                onExit = { screen = "onboarding" },
                onRecommendNickname = {
                    nickname = nextSuggestedNickname(nickname)
                    checked = false
                },
                completed = completed,
                starting = notificationOptIn.waiting,
                onCompletedBack = { screen = "map" },
                onSubmit = { skip ->
                    if (skip) keywords = emptyList()
                    completed = true
                },
                onStart = notificationOptIn.request,
            )
        else -> PreviewMap()
    }
}

// Sample-only generator; the production screen requests suggestions from the server.
private fun nextSuggestedNickname(current: String): String {
    val modifiers = listOf("설레는", "신나는", "느긋한", "다정한", "씩씩한", "행복한")
    val animals = listOf("수달", "토끼", "다람쥐", "고양이", "여우", "판다")
    return modifiers.flatMap { modifier -> animals.map { modifier + it } }.filterNot { it == current }.random()
}

@Composable
private fun PreviewMap() {
    val today = renewalToday()
    val popups =
        remember(today) {
            listOf(
                RenewalPopup("sample-1", "모락절미", listOf("디저트"), "울산 남구", today.minusDays(3), today.plusDays(4)),
                RenewalPopup("sample-2", "두바이쫀득쿠키", listOf("디저트"), "울산 남구", today.minusDays(2), today.plusDays(6)),
                RenewalPopup("sample-3", "가을 캐릭터 마켓", listOf("캐릭터", "굿즈"), "울산 중구", today.minusDays(1), today.plusDays(8)),
                RenewalPopup("sample-4", "두쫀쿠", listOf("디저트"), "울산 남구", today, today.plusDays(3)),
                RenewalPopup("sample-5", "산리오 작은 상점", listOf("캐릭터"), "부산 해운대구", today.plusDays(1), today.plusDays(10)),
                RenewalPopup("sample-6", "향기로 남기는 하루", listOf("뷰티"), "서울 성동구", today.plusDays(4), today.plusDays(14)),
                RenewalPopup("sample-7", "나의 취향을 담은 방", listOf("라이프"), "울산 남구", today.minusDays(2), today.plusDays(2)),
                RenewalPopup("sample-8", "새로운 계절의 옷장", listOf("패션"), "서울 마포구", today.minusDays(5), today.plusDays(7)),
            )
        }
    var favorites by rememberSaveable { mutableStateOf(listOf<String>()) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var sort by rememberSaveable { mutableStateOf("CLOSEST") }
    var region by rememberSaveable { mutableStateOf("전체") }
    var regionsOpen by remember { mutableStateOf(false) }
    val ordered =
        when (sort) {
            "NEWEST" -> popups.sortedByDescending { it.start }
            "CLOSING_SOON" -> popups.sortedBy { it.end }
            else -> popups
        }
    Column(Modifier.fillMaxSize().background(Color.White).systemBarsPadding()) {
        Box(Modifier.weight(1f)) {
            RenewalMapUi(
                ordered.filter { region == "전체" || it.region == region },
                favorites.toSet(),
                {
                    favorites =
                        if (it in
                            favorites
                        ) {
                            favorites - it
                        } else {
                            favorites + it
                        }
                },
                { detailId = it },
                onSortChanged = { sort = it },
                regionButton = {
                    Box {
                        Row(
                            Modifier.clickable { regionsOpen = true }.semantics { contentDescription = "지역 선택" },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(region, style = renewalText(15, true).copy(letterSpacing = (-.3).sp))
                            RenewalIcon(R.drawable.renewal_asset_b7881, size = 14.dp)
                        }
                        DropdownMenu(regionsOpen, { regionsOpen = false }, containerColor = Color.White) {
                            (listOf("전체") + popups.map { it.region }.distinct()).forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            option,
                                            style = renewalText(13),
                                            color =
                                                if (region ==
                                                    option
                                                ) {
                                                    mainOrange
                                                } else {
                                                    Color(0xFF191919)
                                                },
                                        )
                                    },
                                    onClick = {
                                        region = option
                                        regionsOpen = false
                                    },
                                    modifier = Modifier.semantics { contentDescription = "지역 $option" },
                                )
                            }
                        }
                    }
                },
                mapContent = { visible, selected, onSelect -> SampleMap(visible, selected, onSelect) },
            )
        }
        RenewalBottomBar(2) {}
    }
    detailId?.let { id ->
        val popup = popups.first { it.id == id }
        AlertDialog(
            onDismissRequest = { detailId = null },
            title = { Text(popup.name, style = renewalText(20, true)) },
            text = { Text("${popup.region}\n${popup.start} ~ ${popup.end}", style = renewalText()) },
            confirmButton = { TextButton({ detailId = null }) { Text("지도로 돌아가기", color = mainOrange) } },
            containerColor = Color.White,
        )
    }
}

@Composable
private fun SampleMap(
    popups: List<RenewalPopup>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    val context = LocalContext.current
    val density = LocalDensity.current
    val markerRenderer = remember(context) { RenewalMarkerRenderer(context) }
    BoxWithConstraints(Modifier.fillMaxSize().clipToBounds().background(Color(0xFFF3F1EC))) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        Canvas(
            Modifier.fillMaxSize().pointerInput(Unit) {
                detectDragGestures { change, delta ->
                    change.consume()
                    pan += delta
                }
            },
        ) {
            translate(pan.x, pan.y) {
                drawRect(Color(0xFFBFDDF2), topLeft = Offset(-width, -height), size = Size(width * 3, height * 1.14f))
                drawRect(Color(0xFFF3F1EC), topLeft = Offset(-width, height * .14f), size = Size(width * 3, height * 2))
                drawRect(Color(0xFFE6E1F4), topLeft = Offset(width * .64f, height * .44f), size = Size(width * .28f, height * .09f))
                drawRect(Color(0xFFEBE6DA), topLeft = Offset(width * .15f, height * .34f), size = Size(width * .2f, height * .08f))
                repeat(16) { i ->
                    drawLine(
                        Color.White,
                        Offset(-width, height * .14f + i * 80.dp.toPx()),
                        Offset(
                            width * 2,
                            height * .14f + i * 80.dp.toPx(),
                        ),
                        6.dp.toPx(),
                    )
                }
                repeat(14) { i ->
                    drawLine(
                        Color.White,
                        Offset(-width + i * 73.dp.toPx(), height * .14f),
                        Offset(
                            -width + i * 73.dp.toPx(),
                            height * 2,
                        ),
                        7.dp.toPx(),
                    )
                }
                drawLine(Color(0xFFFCE7B2), Offset(width * .56f, height * .14f), Offset(width * .56f, height * 2), 14.dp.toPx())
            }
        }
        Text(
            "태화강",
            style = renewalText(13),
            color = Color(0xFF5C8DB3),
            modifier =
                Modifier.offset {
                    IntOffset(
                        (width * .42f + pan.x).roundToInt(),
                        (
                            height *
                                .07f +
                                pan.y
                        ).roundToInt(),
                    )
                },
        )
        val positions =
            listOf(
                .46f to .43f,
                .39f to .30f,
                .78f to .35f,
                .70f to .51f,
                .12f to .37f,
                .23f to .58f,
                .45f to .67f,
                .82f to .66f,
            )
        popups.forEachIndexed { index, popup ->
            val position = positions[index % positions.size]
            val selected = popup.id == selectedId
            val image = remember(popup.name, selected, selectedId) {
                markerRenderer.popup(popup.name, selected, selectedId != null && !selected)
            }
            Image(
                image.bitmap.asImageBitmap(),
                "지도마커 ${popup.name}",
                Modifier.offset {
                    IntOffset(
                        (width * position.first + pan.x - image.bitmap.width / 2f).roundToInt(),
                        (height * position.second + pan.y - image.bitmap.height * image.anchor.y).roundToInt(),
                    )
                }.size(
                    with(density) { image.bitmap.width.toDp() },
                    with(density) { image.bitmap.height.toDp() },
                ).testTag("marker-${popup.id}").clickable { onSelect(popup.id) },
            )
        }
        if (selectedId == null) IconButton(
            {
                pan = Offset.Zero
            },
            Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = minOf(370.dp, maxHeight * .6f))
                .size(42.dp)
                .background(Color.White, CircleShape),
        ) {
            RenewalIcon(R.drawable.renewal_asset_86d6d, "내 위치", size = 22.dp)
        }
    }
}
