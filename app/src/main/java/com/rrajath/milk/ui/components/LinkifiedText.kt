package com.rrajath.milk.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink

// Scheme-required, plus bare www. -- deliberately conservative (TDD §7.1).
private val URL_REGEX = Regex("""(https?://\S+)|(www\.\S+)""", RegexOption.IGNORE_CASE)

// Row titles are one line, no wrap (PRD §7.1); tapping a link opens it,
// tapping elsewhere on the row enters edit mode (handled by the caller via
// pointerInput -- Compose's own link hit-testing only consumes taps that
// land on the link's glyph run, so non-link taps still reach the row).
@Composable
fun LinkifiedText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val linkColor = com.rrajath.milk.ui.theme.ShoppTheme.colors.accent
    val annotated = remember(text, linkColor) { buildLinkifiedString(text, linkColor) }
    androidx.compose.material3.Text(
        text = annotated,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = onTextLayout,
        modifier = modifier,
    )
}

private fun buildLinkifiedString(text: String, linkColor: Color): AnnotatedString = buildAnnotatedString {
    var lastIndex = 0
    for (match in URL_REGEX.findAll(text)) {
        append(text.substring(lastIndex, match.range.first))
        val url = match.value
        val target = if (url.startsWith("www.", ignoreCase = true)) "https://$url" else url
        withLink(
            LinkAnnotation.Url(
                target,
                TextLinkStyles(style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
            )
        ) {
            append(url)
        }
        lastIndex = match.range.last + 1
    }
    append(text.substring(lastIndex))
}
