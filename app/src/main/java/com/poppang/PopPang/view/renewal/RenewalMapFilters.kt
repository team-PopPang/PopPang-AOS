package com.poppang.PopPang.view.renewal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poppang.PopPang.R
import com.poppang.PopPang.ui.theme.mainOrange
import java.time.format.DateTimeFormatter

@Composable
internal fun FilterShortcuts(
    filters: RenewalMapFilters,
    categories: List<String>,
    onChange: (RenewalMapFilters) -> Unit,
    onAll: () -> Unit,
) {
    var menu by remember { mutableStateOf<String?>(null) }
    var categoryDraft by remember(filters.categories) { mutableStateOf(filters.categories) }
    var showDates by remember { mutableStateOf(false) }
    val shortcutsScroll = rememberScrollState()
    val density = LocalDensity.current
    val windowHeight =
        with(density) {
            LocalWindowInfo.current.containerSize.height
                .toDp()
        }
    val safeInsets = WindowInsets.safeDrawing
    val insetHeight = with(density) { (safeInsets.getTop(density) + safeInsets.getBottom(density)).toDp() }
    val menuMaxHeight = (windowHeight - insetHeight - 96.dp).coerceIn(80.dp, 440.dp)
    val periodChip = remember { BringIntoViewRequester() }
    LaunchedEffect(filters.period, filters.start, filters.end) {
        if (filters.period == PopupPeriod.Custom && filters.start != null && filters.end != null) {
            withFrameNanos { }
            periodChip.bringIntoView()
        }
    }
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Row(Modifier.horizontalScroll(shortcutsScroll), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                onAll,
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.testTag("map-filters"),
            ) {
                Row(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RenewalIcon(R.drawable.renewal_asset_7db3a, "전체 필터", size = 16.dp)
                    if (filters.activeCount > 0) Text(filters.activeCount.toString(), style = renewalText(10), color = mainOrange)
                }
            }
            listOf("카테고리", "기간").forEach { label ->
                Box {
                    var chipWidth by remember { mutableStateOf(0.dp) }
                    var chipBottom by remember { mutableStateOf(0.dp) }
                    val expanded = menu == label
                    val visibility = remember { MutableTransitionState(false) }
                    visibility.targetState = expanded
                    val attached = visibility.currentState || visibility.targetState || !visibility.isIdle
                    val arrowRotation by animateFloatAsState(
                        if (expanded) 180f else 0f,
                        tween(180, easing = FastOutSlowInEasing),
                        label = "filter-arrow-$label",
                    )
                    val selected =
                        when (label) {
                            "카테고리" -> filters.categories.isNotEmpty()
                            else ->
                                filters.period !=
                                    PopupPeriod.All
                        }
                    val text =
                        when (label) {
                            "카테고리" ->
                                if (filters.categories.isEmpty()) {
                                    label
                                } else {
                                    filters.categories.first() +
                                        if (filters.categories.size > 1) " +${filters.categories.size - 1}" else ""
                                }
                            else ->
                                when {
                                    filters.period == PopupPeriod.All -> label
                                    filters.period == PopupPeriod.Custom && filters.start != null && filters.end != null -> {
                                        val format = DateTimeFormatter.ofPattern("yy.MM.dd")
                                        "${filters.start.format(format)} ~ ${filters.end.format(format)}"
                                    }
                                    else -> filters.period.label
                                }
                        }
                    Surface(
                        onClick = {
                            menu = if (expanded) null else label
                            if (!expanded) categoryDraft = filters.categories
                        },
                        modifier =
                            Modifier
                                .onSizeChanged { chipWidth = with(density) { it.width.toDp() } }
                                .onGloballyPositioned { chipBottom = with(density) { it.boundsInWindow().bottom.toDp() } }
                                .semantics { stateDescription = if (expanded) "펼쳐짐" else "접힘" }
                                .testTag(if (label == "기간") "map-period-chip" else "map-filter-chip-$label")
                                .then(
                                    if (label ==
                                        "기간"
                                    ) {
                                        Modifier.bringIntoViewRequester(periodChip)
                                    } else {
                                        Modifier
                                    },
                                ),
                        shape = if (attached) RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp) else RoundedCornerShape(18.dp),
                        border = if (attached) BorderStroke(1.dp, Color(0xFFEBEBEB)) else null,
                        color = Color.White,
                        shadowElevation = if (attached) 0.dp else 2.dp,
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text,
                                style = renewalText(13).copy(letterSpacing = (-.26).sp),
                                color =
                                    if (attached) {
                                        mainOrange
                                    } else if (label == "기간" &&
                                        filters.period == PopupPeriod.Custom
                                    ) {
                                        Color(0xFF333333)
                                    } else if (selected) {
                                        mainOrange
                                    } else {
                                        RenewalInk
                                    },
                            )
                            RenewalIcon(
                                R.drawable.renewal_asset_b7881,
                                size = 14.dp,
                                modifier = Modifier.rotate(arrowRotation),
                            )
                        }
                    }
                    key(label) {
                        ConnectedFilterMenu(
                            label = label,
                            visibility = visibility,
                            anchorWidth = chipWidth,
                            maxHeight = minOf(menuMaxHeight, (windowHeight - chipBottom - insetHeight - 16.dp).coerceAtLeast(80.dp)),
                            onDismiss = { menu = null },
                        ) {
                            when (label) {
                                "카테고리" -> {
                                    CategoryMenu(
                                        categories = categories,
                                        selected = categoryDraft,
                                        onToggle = { category ->
                                            categoryDraft =
                                                if (category in categoryDraft) categoryDraft - category else categoryDraft + category
                                        },
                                        onReset = { categoryDraft = emptySet() },
                                        onApply = {
                                            onChange(filters.copy(categories = categoryDraft))
                                            menu = null
                                        },
                                    )
                                }
                                else ->
                                    PopupPeriod.entries.forEach { period ->
                                        MenuChoice(
                                            if (period ==
                                                PopupPeriod.Custom
                                            ) {
                                                "직접 선택"
                                            } else {
                                                period.label
                                            },
                                            filters.period == period,
                                        ) {
                                            if (period ==
                                                PopupPeriod.Custom
                                            ) {
                                                showDates = true
                                            } else {
                                                onChange(filters.copy(period = period, start = null, end = null))
                                            }
                                            menu = null
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showDates) {
        RenewalDateRangeDialog(filters.start, filters.end, { showDates = false }) { start, end ->
            onChange(filters.copy(period = PopupPeriod.Custom, start = start, end = end))
            showDates = false
        }
    }
}

@Composable
private fun CategoryMenu(
    categories: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).testTag("map-category-list")) {
            categories.forEach { category -> MenuChoice(category, category in selected) { onToggle(category) } }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp).testTag("map-category-actions"),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Surface(
                onClick = onReset,
                modifier = Modifier.width(70.dp).height(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF2F2F2),
            ) {
                Box(contentAlignment = Alignment.Center) { Text("초기화", style = renewalText(13, true), color = Color(0xFF666666)) }
            }
            Surface(
                onClick = onApply,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = mainOrange,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) { Text("${selected.size}개 적용", style = renewalText(13, true), color = Color.White) }
            }
        }
    }
}

@Composable
private fun MenuChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                label,
                style = renewalText(14).copy(fontFamily = RenewalRegular, lineHeight = 17.sp),
                color = if (selected) mainOrange else RenewalInk,
            )
        },
        trailingIcon = {
            if (selected) {
                Text(
                    "✓",
                    style =
                        TextStyle(
                            fontFamily = RenewalInter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 19.sp,
                            lineHeightStyle = RenewalLineHeight,
                        ),
                    color = mainOrange,
                )
            }
        },
        modifier = Modifier.padding(horizontal = 8.dp).height(42.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        onClick = onClick,
    )
}

@Composable
internal fun RenewalFilterSheet(
    initial: RenewalMapFilters,
    categories: List<String>,
    popups: List<RenewalPopup>,
    query: String,
    onDismiss: () -> Unit,
    onApply: (RenewalMapFilters) -> Unit,
) {
    var draft by remember { mutableStateOf(initial) }
    var showDates by remember { mutableStateOf(false) }
    val count = draft.apply(popups, query, renewalToday()).size
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        scrimColor = Color.Black.copy(alpha = .4f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                .testTag("map-filter-sheet"),
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).size(40.dp, 4.dp).background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp)))
            Spacer(Modifier.height(22.dp))
            Row(
                Modifier.fillMaxWidth().height(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("필터", style = renewalNotoText(20, true))
                Text(
                    "초기화",
                    style = renewalNotoText(14),
                    color = Color(0xFF8A8A8A),
                    modifier = Modifier.clickable(role = Role.Button) { draft = RenewalMapFilters() },
                )
            }
            Spacer(Modifier.height(22.dp))
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                FilterGroup("기간") {
                    PopupPeriod.entries.filter { it != PopupPeriod.All }.forEach { period ->
                        FilterChoiceChip(if (period == PopupPeriod.Custom) "날짜 선택" else period.label, draft.period == period) {
                            if (period == PopupPeriod.Custom) {
                                showDates = true
                            } else {
                                draft = draft.copy(period = period, start = null, end = null)
                            }
                        }
                    }
                }
                if (draft.period == PopupPeriod.Custom && draft.start != null && draft.end != null) {
                    Text(
                        "${draft.start} ~ ${draft.end}",
                        style = renewalNotoText(12),
                        color = mainOrange,
                        modifier = Modifier.clickable { showDates = true },
                    )
                }
                FilterGroup("카테고리") {
                    categories.forEach { category ->
                        FilterChoiceChip(category, category in draft.categories) {
                            draft =
                                draft.copy(
                                    categories =
                                        if (category in
                                            draft.categories
                                        ) {
                                            draft.categories - category
                                        } else {
                                            draft.categories + category
                                        },
                                )
                        }
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth().height(51.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    onClick = { draft = RenewalMapFilters() },
                    modifier = Modifier.width(105.dp).fillMaxHeight(),
                    color = Color(0xFFF2F2F2),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("초기화", style = renewalNotoText(13, true), color = Color(0xFF666666))
                    }
                }
                Surface(
                    onClick = { onApply(draft) },
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("map-filter-apply"),
                    color = mainOrange,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("팝업 ${count}개 보기", style = renewalNotoText(16, true), color = Color.White)
                    }
                }
            }
        }
    }
    if (showDates) {
        RenewalDateRangeDialog(draft.start, draft.end, { showDates = false }) { start, end ->
            draft = draft.copy(period = PopupPeriod.Custom, start = start, end = end)
            showDates = false
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    content: @Composable FlowRowScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = renewalNotoText(15, true))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun FilterChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .semantics { this.selected = selected }
            .clickable(role = Role.Checkbox, onClick = onClick)
            .background(if (selected) Color(0xFFFBF1EA) else Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, if (selected) mainOrange else Color(0xFFE3E3E3), RoundedCornerShape(18.dp))
            .padding(horizontal = 15.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (selected) RenewalIcon(R.drawable.renewal_asset_5d022, size = 14.dp)
        Text(label, style = renewalNotoText(14, selected), color = if (selected) mainOrange else Color(0xFF444444))
    }
}
