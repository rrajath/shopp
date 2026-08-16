package com.rrajath.shopp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType

/**
 * TDD §6.2/§7.4: tap checkbox completes (write fires immediately, the local
 * `completing` flag only drives the visual transition); tap elsewhere on the
 * row enters edit mode with the caret placed at the tap position via
 * [androidx.compose.ui.text.TextLayoutResult.getOffsetForPosition].
 */
@Composable
fun ItemRow(
    item: ItemEntity,
    onComplete: () -> Unit,
    onCommitEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    var completing by remember(item.id) { mutableStateOf(false) }
    var editing by remember(item.id) { mutableStateOf(false) }
    var fieldValue by remember(item.id) { mutableStateOf(TextFieldValue(item.title)) }
    var textLayout by remember(item.id) { mutableStateOf<TextLayoutResult?>(null) }
    val focusRequester = remember(item.id) { FocusRequester() }

    fun commitIfChanged() {
        val newTitle = fieldValue.text.trim()
        if (newTitle.isNotEmpty() && newTitle != item.title) onCommitEdit(newTitle)
    }

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = ShoppDimens.tapTarget)
            .padding(
                horizontal = ShoppDimens.rowPaddingHorizontal,
                vertical = ShoppDimens.rowPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.rowGap),
    ) {
        CompleteCheckbox(
            completing = completing,
            onTap = {
                if (!completing) {
                    completing = true
                    onComplete()
                }
            },
        )

        if (editing) {
            var hasBeenFocused by remember(item.id) { mutableStateOf(false) }
            BasicTextField(
                value = fieldValue,
                onValueChange = { fieldValue = it },
                textStyle = ShoppType.itemText.copy(color = colors.foreground),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    editing = false
                    commitIfChanged()
                }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            hasBeenFocused = true
                        } else if (hasBeenFocused && editing) {
                            // A real blur (focus was gained, then lost) -- not
                            // the initial composition, which reports
                            // isFocused=false before requestFocus() runs.
                            editing = false
                            commitIfChanged()
                        }
                    },
            )
            LaunchedEffect(item.id) { focusRequester.requestFocus() }
        } else {
            LinkifiedText(
                text = item.title,
                style = ShoppType.itemText.copy(
                    color = colors.foreground.copy(alpha = if (completing) 0.38f else 1f),
                    textDecoration = if (completing) TextDecoration.LineThrough else TextDecoration.None,
                ),
                onTextLayout = { textLayout = it },
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(item.id, completing) {
                        if (!completing) {
                            detectTapGestures { offset ->
                                val index = textLayout?.getOffsetForPosition(offset) ?: item.title.length
                                fieldValue = TextFieldValue(item.title, TextRange(index))
                                editing = true
                            }
                        }
                    },
            )
        }
    }
}

@Composable
private fun CompleteCheckbox(completing: Boolean, onTap: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ShoppTheme.colors
    val borderColor by animateColorAsState(
        targetValue = if (completing) colors.accent else colors.checkboxBorder,
        animationSpec = tween(180),
        label = "checkboxBorder",
    )
    val fillColor by animateColorAsState(
        targetValue = if (completing) colors.accent else Color.Transparent,
        animationSpec = tween(180),
        label = "checkboxFill",
    )
    val checkAlpha by animateFloatAsState(
        targetValue = if (completing) 1f else 0f,
        animationSpec = tween(140, delayMillis = if (completing) 40 else 0),
        label = "checkAlpha",
    )

    Box(
        modifier = modifier
            .size(ShoppDimens.tapTarget)
            .pointerInput(Unit) { detectTapGestures { onTap() } },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(ShoppDimens.checkboxSize)
                .background(fillColor, CircleShape)
                .border(ShoppDimens.checkboxBorderWidth, borderColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colors.onAccent.copy(alpha = checkAlpha),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
