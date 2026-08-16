package com.rrajath.shopp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType
import com.rrajath.shopp.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    groupByLabel: Boolean,
    keepQuickAddOpen: Boolean,
    confirmBeforeClearing: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onGroupByLabelChange: (Boolean) -> Unit,
    onKeepQuickAddOpenChange: (Boolean) -> Unit,
    onConfirmBeforeClearingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState()),
    ) {
        SectionLabel(text = "Appearance", topPadding = ShoppDimens.settingsSectionLabelPaddingTop)
        ThemeSegmentedControl(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ShoppDimens.labelsTitlePaddingHorizontal),
        )

        SectionLabel(text = "Behaviour", topPadding = ShoppDimens.settingsSectionLabelPaddingTopSecond)
        ToggleRow(
            name = "Group by label",
            hint = "Off shows one flat list in the order you added.",
            checked = groupByLabel,
            onCheckedChange = onGroupByLabelChange,
        )
        ToggleRow(
            name = "Keep Quick Add open",
            hint = "Enter adds the item and leaves the field ready.",
            checked = keepQuickAddOpen,
            onCheckedChange = onKeepQuickAddOpenChange,
        )
        ToggleRow(
            name = "Confirm before clearing",
            hint = "Ask before emptying Recently completed.",
            checked = confirmBeforeClearing,
            onCheckedChange = onConfirmBeforeClearingChange,
        )

        Text(
            text = "Shopp · 1.0",
            style = ShoppType.footerNote.copy(color = colors.muted),
            modifier = Modifier.padding(
                horizontal = ShoppDimens.labelsTitlePaddingHorizontal,
                vertical = ShoppDimens.footerPaddingTop,
            ),
        )
    }
}

@Composable
private fun SectionLabel(text: String, topPadding: androidx.compose.ui.unit.Dp) {
    val colors = ShoppTheme.colors
    Text(
        text = text.uppercase(),
        style = ShoppType.settingsSectionLabel.copy(color = colors.muted),
        modifier = Modifier.padding(
            horizontal = ShoppDimens.labelsTitlePaddingHorizontal,
            vertical = 0.dp,
        ).padding(top = topPadding, bottom = ShoppDimens.settingsSectionLabelPaddingBottom),
    )
}

// Single pill-shaped segmented control (one outer border, no gaps between
// segments) per internal-docs/website/ShoppApp.dc.html's Settings screen --
// was 3 separate bordered buttons.
@Composable
private fun ThemeSegmentedControl(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(ShoppDimens.themeSegmentContainerCornerRadius))
            .border(
                width = 1.dp,
                color = colors.line,
                shape = RoundedCornerShape(ShoppDimens.themeSegmentContainerCornerRadius),
            ),
    ) {
        ThemeSegment("System", themeMode == ThemeMode.SYSTEM, Modifier.weight(1f)) { onThemeModeChange(ThemeMode.SYSTEM) }
        ThemeSegment("Light", themeMode == ThemeMode.LIGHT, Modifier.weight(1f)) { onThemeModeChange(ThemeMode.LIGHT) }
        ThemeSegment("Dark", themeMode == ThemeMode.DARK, Modifier.weight(1f)) { onThemeModeChange(ThemeMode.DARK) }
    }
}

@Composable
private fun ThemeSegment(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = ShoppTheme.colors
    Box(
        modifier = modifier
            .background(if (selected) colors.accent else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = ShoppDimens.themeSegmentPaddingVertical),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = ShoppType.settingsButtonLabel.copy(
                color = if (selected) colors.onAccent else colors.muted,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun ToggleRow(name: String, hint: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = ShoppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(
                horizontal = ShoppDimens.labelsTitlePaddingHorizontal,
                vertical = ShoppDimens.toggleRowPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.toggleRowGap),
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = name, style = ShoppType.toggleName.copy(color = colors.foreground))
            Text(text = hint, style = ShoppType.toggleHint.copy(color = colors.muted))
        }
        ToggleSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ToggleSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = ShoppTheme.colors
    val trackColor by animateColorAsState(if (checked) colors.accent else Color.Transparent, tween(180), label = "trackColor")
    val borderColor by animateColorAsState(if (checked) colors.accent else colors.checkboxBorder, tween(180), label = "borderColor")
    val knobColor = if (checked) colors.onAccent else colors.checkboxBorder
    val knobSize by animateDpAsState(if (checked) ShoppDimens.toggleKnobSizeOn else ShoppDimens.toggleKnobSizeOff, label = "knobSize")
    val knobOffsetX by animateDpAsState(if (checked) 23.dp else 5.dp, label = "knobOffsetX")
    val knobOffsetY = if (checked) 3.dp else 5.dp

    Box(
        modifier = Modifier
            .size(ShoppDimens.toggleTrackWidth, ShoppDimens.toggleTrackHeight)
            .clip(RoundedCornerShape(ShoppDimens.toggleTrackCornerRadius))
            .background(trackColor)
            .border(
                ShoppDimens.toggleTrackBorderWidth,
                borderColor,
                RoundedCornerShape(ShoppDimens.toggleTrackCornerRadius),
            )
            .clickable { onCheckedChange(!checked) },
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffsetX, y = knobOffsetY)
                .size(knobSize)
                .background(knobColor, CircleShape),
        )
    }
}
