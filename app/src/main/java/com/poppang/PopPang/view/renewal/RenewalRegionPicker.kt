package com.poppang.PopPang.view.renewal

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.poppang.PopPang.R
import com.poppang.PopPang.model.RegionsResponse
import com.poppang.PopPang.ui.theme.Bold17
import com.poppang.PopPang.ui.theme.Medium12
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray2
import com.poppang.PopPang.ui.theme.mainGray3
import com.poppang.PopPang.ui.theme.mainGray4
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainOrange

@Composable
internal fun RenewalRegionButton(
    selectedRegion: String,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .semantics {
                contentDescription = "지역 선택"
                stateDescription = if (expanded) "펼쳐짐" else "접힘"
            }.clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(selectedRegion, style = renewalText(15, true), color = mainBlack)
        Icon(
            painterResource(R.drawable.renewal_asset_b7881),
            null,
            Modifier.padding(start = 2.dp).size(14.dp).rotate(if (expanded) 180f else 0f),
            tint = mainGray2,
        )
    }
}

/** Use half of the screen content and keep the navigation menu outside the overlay. */
@Composable
internal fun RenewalRegionPicker(
    visible: Boolean,
    selectedRegion: String,
    regions: List<RegionsResponse>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(visible, onBack = onDismiss)
    BoxWithConstraints(modifier.fillMaxSize().clipToBounds()) {
        val sheetHeight = maxHeight * .5f
        val scrimAlpha by animateFloatAsState(if (visible) .32f else 0f, tween(150), label = "region-scrim")
        val interaction = remember { MutableInteractionSource() }
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .then(
                    if (visible) Modifier.clickable(interactionSource = interaction, indication = null, onClick = onDismiss) else Modifier,
                ).clearAndSetSemantics {},
        )
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter,
                    ).widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .fillMaxWidth()
                    .height(sheetHeight),
            enter = slideInVertically(tween(240, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(150)),
            exit = slideOutVertically(tween(200, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(130)),
        ) {
            val regionMap =
                remember(regions) {
                    val base = regions.associate { it.region to it.districtList }
                    linkedMapOf("전체" to listOf("전체")).apply {
                        base["서울"]?.let { put("서울", it) }
                        base.forEach { (region, districts) -> putIfAbsent(region, districts) }
                    }
                }
            var currentRegion by remember { mutableStateOf(selectedRegion) }
            LaunchedEffect(visible, selectedRegion) { if (visible) currentRegion = selectedRegion }
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .testTag("map-region-panel")
                        .semantics { paneTitle = "지역 선택" }
                        .then(if (!visible) Modifier.clearAndSetSemantics {} else Modifier)
                        .pointerInput(Unit) {},
                color = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            ) {
                Column(Modifier.fillMaxSize().padding(horizontal = 28.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 16.dp).testTag("map-region-header"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("지역", style = Bold17, color = mainBlack)
                        IconButton(onDismiss, enabled = visible, modifier = Modifier.size(48.dp).testTag("map-region-close")) {
                            Icon(painterResource(R.drawable.close_icon), "닫기", Modifier.size(24.dp), tint = mainBlack)
                        }
                    }
                    HorizontalDivider(color = mainGray3)
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            Modifier
                                .width(65.dp)
                                .fillMaxHeight()
                                .background(mainGray4)
                                .testTag("map-region-provinces"),
                        ) {
                            items(regionMap.keys.toList()) { region ->
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .background(if (currentRegion == region) Color.White else mainGray4)
                                        .selectable(currentRegion == region, enabled = visible, role = Role.RadioButton) {
                                            currentRegion =
                                                region
                                        }.padding(horizontal = 4.dp, vertical = 8.dp)
                                        .testTag("map-region-province-$region"),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(region, style = Medium12, color = if (currentRegion == region) mainOrange else mainGray1)
                                }
                            }
                        }
                        VerticalDivider(Modifier.fillMaxHeight(), color = mainGray5)
                        key(currentRegion) {
                            LazyColumn(Modifier.weight(1f).fillMaxHeight().testTag("map-region-districts")) {
                                items(regionMap[currentRegion] ?: listOf("전체")) { district ->
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .clickable(enabled = visible, role = Role.Button) {
                                                onSelect(if (district == "전체") currentRegion else "$currentRegion $district")
                                                onDismiss()
                                            }.padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("map-region-district-$district"),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        Text(district, style = Medium12, color = mainBlack)
                                    }
                                    HorizontalDivider(color = mainGray5)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = mainGray3)
                }
            }
        }
    }
}
