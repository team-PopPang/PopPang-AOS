package com.poppang.PopPang.view.renewal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.poppang.PopPang.R
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.view.MapSheetScaffold
import com.poppang.PopPang.view.MapSheetValue
import com.poppang.PopPang.view.rememberMapSheetState
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

private val RenewalMapFiltersSaver =
    listSaver<RenewalMapFilters, String>(
        // Keep the removed filter slots so existing category selections can still be restored.
        save = { listOf("All", "All", "", "") + it.categories },
        restore = ::restoreRenewalMapFilters,
    )

internal fun renewalMapMiddleFraction(viewportHeight: Dp): Float =
    if (viewportHeight <= 0.dp) .30f else ((viewportHeight - 446.dp) / viewportHeight).coerceIn(.30f, .65f)

@Composable
internal fun RenewalMapUi(
    popups: List<RenewalPopup>,
    favoriteIds: Set<String>,
    onFavorite: (String) -> Unit,
    onDetail: (String) -> Unit,
    categories: List<String> = RenewalCategories,
    onSortChanged: (String) -> Unit = {},
    filteredPopups: List<RenewalPopup>? = null,
    onFilterChanged: (Set<String>, String) -> Unit = { _, _ -> },
    regionButton: @Composable () -> Unit = { Text("전체", style = renewalText(13, true)) },
    mapContent: @Composable (visible: List<RenewalPopup>, selectedId: String?, onSelect: (String) -> Unit) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var filters by rememberSaveable(stateSaver = RenewalMapFiltersSaver) { mutableStateOf(RenewalMapFilters()) }
    var showFilter by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable { mutableStateOf("가까운순") }
    val today = renewalToday()
    LaunchedEffect(filters.categories, query) { onFilterChanged(filters.categories, query.trim()) }
    val visible = remember(popups, filteredPopups, filters, query, today) {
        filteredPopups?.filterNot { it.end.isBefore(today) } ?: filters.apply(popups, query, today)
    }
    val categoryCounts = remember(popups, query, today) { renewalCategoryCounts(popups, query, today) }
    val selected = visible.firstOrNull { it.id == selectedId }
    val sheet = rememberMapSheetState()
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val expanded = sheet.settledValue == MapSheetValue.Expanded
    LaunchedEffect(visible.map { it.id }) { if (selectedId != null && selected == null) selectedId = null }
    BackHandler(selected != null || expanded) {
        selectedId = null
        scope.launch { sheet.animateTo(MapSheetValue.Middle) }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        BoxWithConstraints(Modifier.weight(1f)) {
            val middleFraction = renewalMapMiddleFraction(maxHeight)
            val expandedFraction = (1f - 118.dp / maxHeight).coerceIn(.60f, .90f)
            MapSheetScaffold(
                sheetState = sheet,
                expandedHeightFraction = expandedFraction,
                middleHeightFraction = middleFraction,
                renewalAppearance = true,
                sheetHeader = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = if (expanded) 40.dp else 37.dp,
                            ).padding(
                                horizontal = 16.dp,
                            ).padding(
                                top = if (expanded) 0.dp else 12.dp,
                                bottom = if (expanded) 0.dp else 4.dp,
                            ).testTag("map-result-count")
                            .semantics(mergeDescendants = true) {
                                stateDescription =
                                    "${visible.size}개"
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "주변 팝업",
                            style = renewalText(18, true).copy(lineHeight = if (expanded) 22.sp else 21.sp, letterSpacing = (-.36).sp),
                        )
                        if (expanded) {
                            Text(
                                " ${visible.size}",
                                style = renewalText(18, true),
                                color = mainOrange,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        SortMenu(sort) { label, code ->
                            sort = label
                            onSortChanged(code)
                        }
                    }
                    Spacer(Modifier.height(if (expanded) 6.dp else 10.dp))
                },
                sheetContent = {
                    if (visible.isEmpty()) {
                        RenewalEmptyState("조건에 맞는 팝업이 없어요", "검색어나 필터를 바꿔서 찾아보세요") {
                            query = ""
                            filters = RenewalMapFilters()
                        }
                    } else {
                        Box(Modifier.fillMaxSize()) {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = if (expanded) 78.dp else 0.dp),
                                modifier = Modifier.fillMaxSize().testTag("renewal-map-list"),
                            ) {
                                items(visible, key = { it.id }) { popup ->
                                    PopupRow(popup, popup.id in favoriteIds, { onFavorite(popup.id) }, {
                                        selectedId = popup.id
                                        scope.launch { sheet.animateTo(MapSheetValue.Hidden) }
                                    }, Modifier.fillMaxWidth().padding(vertical = 14.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                                }
                            }
                            if (expanded) {
                                MapToggleButton(
                                    "지도 보기",
                                    R.drawable.renewal_asset_84cce,
                                    Modifier.align(Alignment.BottomCenter).padding(bottom = 27.dp),
                                    dark = true,
                                ) {
                                    scope.launch { sheet.animateTo(MapSheetValue.Middle) }
                                }
                            }
                        }
                    }
                },
                content = {
                    mapContent(visible, selectedId) { id ->
                        selectedId = id
                        focus.clearFocus()
                        scope.launch { sheet.animateTo(MapSheetValue.Hidden) }
                    }
                    if (expanded) Box(Modifier.fillMaxSize().background(Color.White))
                },
            )
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Surface(shape = RoundedCornerShape(14.dp), shadowElevation = 4.dp, color = Color.White) {
                    Row(
                        Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                            RenewalIcon(R.drawable.renewal_asset_c4bab, size = 16.dp)
                            regionButton()
                        }
                        VerticalDivider(Modifier.height(20.dp).width(1.dp), color = Color(0xFFE5E5E5))
                        androidx.compose.foundation.text.BasicTextField(
                            query,
                            { query = it },
                            Modifier.weight(1f).testTag("map-search"),
                            singleLine = true,
                            textStyle = renewalText(15).copy(fontFamily = RenewalLight, letterSpacing = (-.3).sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions =
                                KeyboardActions(onSearch = {
                                    focus.clearFocus()
                                    scope.launch { sheet.animateTo(MapSheetValue.Expanded) }
                                }),
                            decorationBox = { inner ->
                                if (query.isEmpty()) {
                                    Text(
                                        "팝업, 브랜드, 지역 검색",
                                        style = renewalText(15).copy(fontFamily = RenewalLight, letterSpacing = (-.3).sp),
                                        color = Color(0xFFAAAAAA),
                                    )
                                }
                                inner()
                            },
                        )
                        IconButton({
                            focus.clearFocus()
                            scope.launch { sheet.animateTo(MapSheetValue.Expanded) }
                        }, Modifier.size(20.dp)) {
                            RenewalIcon(R.drawable.renewal_asset_00818, "검색", size = 20.dp)
                        }
                    }
                }
                Box(Modifier.padding(start = 3.dp)) {
                    FilterShortcuts(filters, categories, { filters = it }, { showFilter = true }, categoryCounts)
                }
            }
            if (sheet.settledValue == MapSheetValue.Hidden) {
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    MapToggleButton("목록 보기", R.drawable.renewal_asset_72354, Modifier.offset(x = 4.dp)) {
                        selectedId = null
                        scope.launch { sheet.animateTo(MapSheetValue.Expanded) }
                    }
                    if (selected !=
                        null
                    ) {
                        PopupPreview(selected, selected.id in favoriteIds, { onFavorite(selected.id) }, { onDetail(selected.id) }) {
                            selectedId = null
                            scope.launch { sheet.animateTo(MapSheetValue.Middle) }
                        }
                    }
                }
            }
        }
    }
    if (showFilter) {
        RenewalFilterSheet(filters, categories, popups, query, onDismiss = { showFilter = false }) {
            filters = it
            selectedId = null
            showFilter = false
            scope.launch { sheet.animateTo(MapSheetValue.Middle) }
        }
    }
}

@Composable
private fun PopupRow(
    popup: RenewalPopup,
    liked: Boolean,
    onLike: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    close: (() -> Unit)? = null,
) {
    val compact = close != null
    val imageHeight = if (compact) 92.dp else 100.dp
    Box(modifier.clickable(onClick = onClick)) {
        Row(horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)) {
            Box(
                Modifier
                    .size(if (compact) 72.dp else 76.dp, imageHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF4B04A)),
                contentAlignment = Alignment.Center,
            ) {
                if (popup.imageUrl != null) {
                    AsyncImage(popup.imageUrl, popup.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Text("[사진]", style = renewalText(11), color = Color.White)
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .height(imageHeight)
                    .clipToBounds()
                    .padding(vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RenewalTag(
                        popup.status(renewalToday()),
                        if (popup.start.isAfter(renewalToday())) Color(0xFFD9530B) else Color(0xFF089700),
                        if (popup.start.isAfter(renewalToday())) Color(0xFFFDEFE6) else Color(0xFFF4FFEE),
                    )
                    popup.categories.firstOrNull()?.let { category ->
                        val rgb = popup.categoryColor?.removePrefix("#")?.takeIf { it.length == 6 }?.toIntOrNull(16)
                        val color = rgb?.let { Color(0xFF000000.toInt() or it) } ?: Color(0xFFD9530B)
                        RenewalTag(category, color, color.copy(alpha = .10f))
                    }
                    if (popup.region.isNotBlank()) RenewalTag(popup.region, Color(0xFFAAAAAA), Color(0xFFF4F4F4))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        popup.name,
                        style = renewalText(16, true).copy(lineHeight = 22.sp, letterSpacing = (-.32).sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        popup.start.format(DateTimeFormatter.ofPattern("yy.MM.dd")) + " ~" +
                            popup.end.format(DateTimeFormatter.ofPattern("yy.MM.dd")),
                        style = renewalText(13).copy(fontFamily = RenewalRegular, letterSpacing = (-.65).sp),
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
            if (!compact) IconButton(onLike, Modifier.size(36.dp)) { HeartIcon(liked) }
        }
        if (close != null) {
            IconButton(close, Modifier.align(Alignment.TopEnd).size(20.dp)) {
                RenewalIcon(R.drawable.renewal_asset_d7166, "선택 닫기", size = 20.dp)
            }
        }
    }
}

@Composable
private fun PopupPreview(
    popup: RenewalPopup,
    liked: Boolean,
    onLike: () -> Unit,
    onDetail: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        Modifier
            .padding(start = 14.dp, end = 18.dp)
            .widthIn(max = 358.dp)
            .fillMaxWidth()
            .testTag("map-preview"),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PopupRow(popup, liked, onLike, onDetail, Modifier.fillMaxWidth(), close = onClose)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onLike,
                    Modifier.size(43.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RenewalLine),
                ) { HeartIcon(liked) }
                Surface(
                    onClick = onDetail,
                    modifier = Modifier.weight(1f).height(43.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = mainOrange,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("상세보기", style = renewalNotoText(16, true), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeartIcon(liked: Boolean) {
    Icon(
        androidx.compose.ui.res
            .painterResource(if (liked) R.drawable.heart_icon else R.drawable.renewal_asset_33f2d),
        if (liked) "찜 해제" else "찜하기",
        Modifier.size(20.dp),
        tint = if (liked) mainOrange else Color.Unspecified,
    )
}

@Composable
internal fun MapToggleButton(
    label: String,
    icon: Int,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick,
        modifier
            .then(
                if (dark) Modifier.widthIn(min = 111.dp).heightIn(min = 39.dp) else Modifier.heightIn(min = 34.dp),
            ).testTag("map-toggle-$label"),
        shape = RoundedCornerShape(20.dp),
        color = if (dark) RenewalInk else Color.White,
        shadowElevation = 4.dp,
    ) {
        Row(
            Modifier.padding(start = if (dark) 16.dp else 12.dp, end = if (dark) 18.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (dark) {
                Icon(
                    androidx.compose.ui.res
                        .painterResource(R.drawable.map_icon_white),
                    null,
                    Modifier.size(17.dp).testTag("map-toggle-icon-$label"),
                    tint = Color.White,
                )
            } else {
                RenewalIcon(icon, size = 16.dp, modifier = Modifier.testTag("map-toggle-icon-$label"))
            }
            Text(
                label,
                style = renewalNotoText(13, true).copy(lineHeight = if (dark) 17.sp else 15.6.sp),
                maxLines = 1,
                softWrap = false,
                color = if (dark) Color.White else RenewalInk,
            )
        }
    }
}

@Composable
private fun SortMenu(
    selected: String,
    onSelected: (String, String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    val options =
        listOf(
            "가까운순" to "CLOSEST",
            "최신순" to "NEWEST",
            "인기순" to "MOST_FAVORITED",
            "조회순" to "MOST_VIEWED",
            "마감임박순" to "CLOSING_SOON",
        )
    Box {
        Row(
            Modifier.clickable { open = true },
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                selected,
                style = renewalText(13).copy(letterSpacing = (-.26).sp),
                color = Color(0xFF555555),
                maxLines = 1,
                softWrap = false,
            )
            RenewalIcon(R.drawable.renewal_asset_b7881, size = 14.dp)
        }
        DropdownMenu(open, { open = false }, containerColor = Color.White) {
            options.forEach { (label, code) ->
                DropdownMenuItem(text = {
                    Text(
                        label,
                        style = renewalText(13),
                        color =
                            if (label ==
                                selected
                            ) {
                                mainOrange
                            } else {
                                RenewalInk
                            },
                    )
                }, onClick = {
                    onSelected(label, code)
                    open = false
                })
            }
        }
    }
}
