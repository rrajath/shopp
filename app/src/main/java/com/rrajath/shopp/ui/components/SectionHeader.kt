package com.rrajath.shopp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType

// Small bold uppercase micro-label with the label's identity color applied
// directly to the text, per ShoppApp.dc.html -- not the large serif display
// heading this used to be (that was the old prototype's treatment; the new
// one resolves the previously-noted deviation from PRD §11's "smaller/
// heavier" micro-header).
@Composable
fun SectionHeader(name: String, color: Color, modifier: Modifier = Modifier) {
    val colors = ShoppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background) // opaque while sticky
            .padding(
                top = ShoppDimens.sectionHeaderPaddingTop,
                start = ShoppDimens.sectionHeaderPaddingHorizontal,
                end = ShoppDimens.sectionHeaderPaddingHorizontal,
                bottom = ShoppDimens.sectionHeaderPaddingBottom,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name.uppercase(),
            style = ShoppType.sectionHeader.copy(color = color),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
