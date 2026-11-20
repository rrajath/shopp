package com.rrajath.shopp.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.rrajath.shopp.designsystem.theme.ShoppDimens
import com.rrajath.shopp.designsystem.theme.ShoppTheme
import com.rrajath.shopp.designsystem.theme.ShoppType

/**
 * `−  n  +` control. Buttons are chip-colored circles inside 44dp tap
 * targets; the end that's hit [min]/[max] dims and stops responding.
 *
 * The buttons are plain clickables, which don't take focus on touch, so
 * tapping them while a text field is being edited keeps the keyboard up
 * and the field focused (ItemRow relies on this to not end its edit).
 */
@Composable
fun QuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    Row(
        modifier = modifier.semantics { contentDescription = "Quantity $value" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(
            icon = Icons.Rounded.Remove,
            description = "Decrease quantity",
            enabled = value > min,
            onClick = { onValueChange(value - 1) },
        )
        Text(
            text = value.toString(),
            style = ShoppType.quantityCount.copy(color = colors.foreground),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = ShoppDimens.quantityStepperCountMinWidth),
        )
        StepperButton(
            icon = Icons.Rounded.Add,
            description = "Increase quantity",
            enabled = value < max,
            onClick = { onValueChange(value + 1) },
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = ShoppTheme.colors
    Box(
        modifier = Modifier
            .size(ShoppDimens.tapTarget)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(ShoppDimens.quantityStepperButtonSize)
                .alpha(if (enabled) 1f else 0.38f)
                .background(colors.chipUnselectedFill, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = colors.chipUnselectedText,
                modifier = Modifier.size(ShoppDimens.quantityStepperIconSize),
            )
        }
    }
}
