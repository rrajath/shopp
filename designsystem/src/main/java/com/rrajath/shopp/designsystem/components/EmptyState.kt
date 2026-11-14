package com.rrajath.shopp.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rrajath.shopp.designsystem.theme.ShoppDimens
import com.rrajath.shopp.designsystem.theme.ShoppTheme
import com.rrajath.shopp.designsystem.theme.ShoppType

// A single left-aligned primary line ("Nothing on the belt.") with no
// heading treatment, plus a secondary hint line below it styled smaller and
// muted, since that hint carries real product information (e.g. how to
// reach Quick Add).
@Composable
fun EmptyState(title: String, body: String, modifier: Modifier = Modifier) {
    val colors = ShoppTheme.colors
    Column(
        modifier = modifier.padding(
            top = ShoppDimens.emptyStatePaddingTop,
            start = ShoppDimens.emptyStatePaddingHorizontal,
            end = ShoppDimens.emptyStatePaddingHorizontal,
        ),
    ) {
        Text(
            text = title,
            style = ShoppType.emptyTitle.copy(color = colors.muted),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(ShoppDimens.emptyStateGap))
        Text(
            text = body,
            style = ShoppType.emptyBody.copy(color = colors.muted),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
