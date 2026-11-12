package com.poppang.PopPang.view.renewal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poppang.PopPang.R
import com.poppang.PopPang.ui.theme.mainOrange

@Composable
internal fun RenewalBottomBar(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    val labels = listOf("홈", "캘린더", "팝팡지도", "팝팡", "마이")
    val icons =
        listOf(
            R.drawable.renewal_asset_392e6,
            R.drawable.renewal_asset_8d345,
            R.drawable.renewal_asset_84cce,
            R.drawable.renewal_asset_d2351,
            R.drawable.renewal_asset_2047a,
        )
    Box(Modifier.fillMaxWidth().heightIn(min = 58.dp).background(Color.White)) {
        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 9.dp, bottom = 5.dp).selectableGroup()) {
            labels.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val color = if (selected) mainOrange else Color(0xFF9A9A9A)
                Column(
                    Modifier.weight(1f).selectable(selected, role = Role.Tab) { onSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(painterResource(icons[index]), null, Modifier.size(24.dp), tint = color)
                    Text(
                        label,
                        modifier = Modifier.fillMaxWidth(),
                        style = renewalNotoText(11, selected).copy(lineHeight = 13.sp),
                        color = color,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
