package com.rrajath.shopp.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.rrajath.shopp.R
import com.rrajath.shopp.capture.CaptureActivity
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.ui.ListSection
import com.rrajath.shopp.ui.theme.ShoppColors
import com.rrajath.shopp.ui.theme.ShoppDimens

@Composable
fun ShoppWidgetContent(
    colors: ShoppColors,
    cardAlpha: Float,
    sections: List<ListSection>,
    hiddenCount: Int,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = cardAlpha))
            .cornerRadius(ShoppDimens.cardCornerRadius),
    ) {
        HeaderRow(colors = colors)

        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = ShoppDimens.headerHorizontalPadding)
                .background(colors.line),
        ) {}

        if (sections.isEmpty()) {
            Text(
                text = "Nothing left. Nice.",
                style = TextStyle(color = ColorProvider(colors.muted), fontSize = 15.sp),
                modifier = GlanceModifier.padding(
                    horizontal = ShoppDimens.sectionHeaderPaddingHorizontal,
                    vertical = 16.dp,
                ),
            )
        } else {
            sections.forEach { section ->
                val sectionColor = if (section.colorIndex == null) {
                    colors.inboxTint
                } else {
                    colors.labelPalette[section.colorIndex % colors.labelPalette.size]
                }
                SectionHeaderRow(name = section.name, color = sectionColor)
                section.items.forEach { item ->
                    ItemRow(
                        item = item,
                        foreground = colors.foreground,
                        checkboxBorder = colors.checkboxBorder,
                        cardBackground = colors.background.copy(alpha = cardAlpha),
                    )
                }
            }
            if (hiddenCount > 0) {
                Text(
                    text = if (hiddenCount == 1) "1 more item" else "$hiddenCount more items",
                    style = TextStyle(color = ColorProvider(colors.muted), fontSize = 12.sp),
                    modifier = GlanceModifier.padding(
                        horizontal = ShoppDimens.sectionHeaderPaddingHorizontal,
                        vertical = 6.dp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HeaderRow(colors: ShoppColors) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 10.dp, top = 12.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Shopp",
            style = TextStyle(
                color = ColorProvider(colors.foreground),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = GlanceModifier.defaultWeight(),
        )
        Box(
            modifier = GlanceModifier
                .size(40.dp)
                .clickable(actionStartActivity(Intent(context, CaptureActivity::class.java))),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_tile_add),
                contentDescription = "Add item",
                colorFilter = ColorFilter.tint(ColorProvider(colors.accent)),
                modifier = GlanceModifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun SectionHeaderRow(name: String, color: Color) {
    Text(
        text = name.uppercase(),
        style = TextStyle(
            color = ColorProvider(color),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        ),
        modifier = GlanceModifier.padding(
            start = ShoppDimens.sectionHeaderPaddingHorizontal,
            end = ShoppDimens.sectionHeaderPaddingHorizontal,
            top = ShoppDimens.sectionHeaderPaddingTop,
            bottom = ShoppDimens.sectionHeaderPaddingBottom,
        ),
    )
}

@Composable
private fun ItemRow(item: ItemEntity, foreground: Color, checkboxBorder: Color, cardBackground: Color) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(ShoppDimens.tapTarget)
            .padding(horizontal = ShoppDimens.rowPaddingHorizontal)
            .clickable(
                actionRunCallback<CompleteItemAction>(actionParametersOf(ItemIdKey to item.id)),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Glance/RemoteViews has no stroke-only border modifier -- a ring is
        // faked with a filled outer circle (the border color) behind a
        // slightly smaller inner circle painted the card's own background,
        // matching ItemRow's checkboxBorderWidth visually.
        Box(
            modifier = GlanceModifier
                .size(ShoppDimens.checkboxSize)
                .cornerRadius(ShoppDimens.checkboxSize / 2)
                .background(checkboxBorder),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = GlanceModifier
                    .size(ShoppDimens.checkboxSize - ShoppDimens.checkboxBorderWidth * 2)
                    .cornerRadius((ShoppDimens.checkboxSize - ShoppDimens.checkboxBorderWidth * 2) / 2)
                    .background(cardBackground),
            ) {}
        }
        Spacer(modifier = GlanceModifier.width(ShoppDimens.rowGap))
        Text(
            text = item.title,
            maxLines = 1,
            style = TextStyle(color = ColorProvider(foreground), fontSize = 15.sp),
        )
    }
}
