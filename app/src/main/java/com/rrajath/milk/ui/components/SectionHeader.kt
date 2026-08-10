package com.rrajath.milk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.rrajath.milk.ui.theme.ShoppDimens
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType

@Composable
fun SectionHeader(name: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = name,
        style = ShoppType.sectionHeader.copy(color = color),
        modifier = modifier
            .fillMaxWidth()
            .background(ShoppTheme.colors.background) // opaque while sticky
            .padding(
                top = ShoppDimens.sectionHeaderPaddingTop,
                start = ShoppDimens.sectionHeaderPaddingHorizontal,
                end = ShoppDimens.sectionHeaderPaddingHorizontal,
                bottom = ShoppDimens.sectionHeaderPaddingBottom,
            ),
    )
}
