package com.poppang.PopPang.view.renewal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poppang.PopPang.R
import com.poppang.PopPang.ui.theme.mainOrange

internal val RenewalInk = Color(0xFF1A1A1A)
internal val RenewalMuted = Color(0xFF8C8C8C)
internal val RenewalPeach = Color(0xFFFFF3E8)
internal val RenewalLine = Color(0xFFE0E0E0)
internal val RenewalMedium = FontFamily(Font(R.font.scdream5))
internal val RenewalBold = FontFamily(Font(R.font.scdream7))
internal val RenewalRegular = FontFamily(Font(R.font.scdream4))
internal val RenewalLight = FontFamily(Font(R.font.scdream3))
internal val RenewalSemiBold = FontFamily(Font(R.font.scdream6))
internal val RenewalLineHeight = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)

@OptIn(ExperimentalTextApi::class)
internal val RenewalInter =
    FontFamily(
        Font(R.font.inter_variable, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
        Font(R.font.inter_variable, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
        Font(R.font.inter_variable, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
        Font(R.font.inter_variable, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    )

@OptIn(ExperimentalTextApi::class)
internal val RenewalNotoMedium =
    FontFamily(
        Font(R.font.noto_sans_kr_variable, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    )

@OptIn(ExperimentalTextApi::class)
internal val RenewalNoto =
    FontFamily(
        Font(R.font.noto_sans_kr_variable, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
        Font(R.font.noto_sans_kr_variable, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    )

internal fun renewalText(
    size: Int = 14,
    bold: Boolean = false,
    lineHeight: Float = 1.2f,
): TextStyle =
    TextStyle(
        fontFamily = if (bold) RenewalBold else RenewalMedium,
        fontSize = size.sp,
        lineHeight = (size * lineHeight).sp,
        lineHeightStyle = RenewalLineHeight,
        color = RenewalInk,
    )

internal fun renewalNotoText(
    size: Int = 14,
    bold: Boolean = false,
): TextStyle =
    TextStyle(
        fontFamily = RenewalNoto,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
        fontSize = size.sp,
        lineHeight = (size * 1.2f).sp,
        lineHeightStyle = RenewalLineHeight,
        letterSpacing = (-size * .02f).sp,
        color = RenewalInk,
    )

@Composable
internal fun RenewalIcon(
    @DrawableRes resource: Int,
    description: String? = null,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier,
) {
    Image(painterResource(resource), description, modifier.size(size))
}

@Composable
internal fun RenewalPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    arrow: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = mainOrange,
                disabledContainerColor = Color(0xFFEEEEEE),
                disabledContentColor = RenewalMuted,
            ),
    ) {
        Text(label, style = renewalText(15), color = if (enabled) Color.White else RenewalMuted)
        if (arrow) {
            Spacer(Modifier.width(10.dp))
            RenewalIcon(R.drawable.renewal_asset_2bac2, size = 24.dp)
        }
    }
}

@Composable
internal fun RenewalChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    trailing: String? = null,
    checkMark: Boolean = true,
    selectedOutline: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .semantics { this.selected = selected }
                .background(if (selected) RenewalPeach else Color.White, RoundedCornerShape(19.dp))
                .border(
                    1.dp,
                    if (selected) {
                        if (selectedOutline) mainOrange else RenewalPeach
                    } else {
                        RenewalLine
                    },
                    RoundedCornerShape(19.dp),
                ).clickable(role = Role.Checkbox, onClick = onClick)
                .padding(horizontal = if (compact) 12.dp else 15.dp, vertical = if (compact) 6.dp else 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (selected && checkMark) Text("✓", color = mainOrange, style = renewalText(14, true))
        Text(
            label,
            style =
                renewalText(if (compact) 12 else 14, selected).let {
                    if (selected) it else it.copy(fontFamily = RenewalLight)
                },
            color = if (selected) mainOrange else Color(0xFF333333),
        )
        if (trailing !=
            null
        ) {
            Text(trailing, style = renewalText(if (compact) 12 else 14, selected), color = if (selected) mainOrange else RenewalMuted)
        }
    }
}

@Composable
internal fun RenewalEmptyState(
    title: String,
    description: String,
    onReset: (() -> Unit)? = null,
) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = renewalText(16, true))
        Text(description, style = renewalText(12), color = RenewalMuted)
        if (onReset != null) TextButton(onClick = onReset) { Text("필터 초기화", color = mainOrange, style = renewalText()) }
    }
}

@Composable
internal fun RenewalTag(
    label: String,
    color: Color,
    background: Color = Color(0xFFF5F5F5),
) {
    Text(
        label,
        style = renewalText(11).copy(letterSpacing = (-.11).sp),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.background(background, RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
