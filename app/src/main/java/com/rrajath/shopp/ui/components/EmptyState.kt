package com.rrajath.shopp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType

// ShoppApp.dc.html depicts this as a single left-aligned 16px line
// ("Nothing on the belt."): `padding: 34px 22px`, text at 50% opacity, no
// heading treatment. The app still shows a secondary hint line below it (the
// prototype has no second line to source one from) styled smaller and
// muted, since that hint carries real product information (e.g. how to
// reach Quick Add) that the single-line prototype text doesn't need to say.
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
