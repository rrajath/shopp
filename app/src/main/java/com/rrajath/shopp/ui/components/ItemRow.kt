package com.rrajath.shopp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.designsystem.components.LinkifiedText
import com.rrajath.shopp.designsystem.components.QuantityStepper
import com.rrajath.shopp.domain.MAX_QUANTITY
import com.rrajath.shopp.domain.MIN_QUANTITY
import com.rrajath.shopp.domain.titleWithQuantity
import com.rrajath.shopp.designsystem.theme.ShoppDimens
import com.rrajath.shopp.designsystem.theme.ShoppTheme
import com.rrajath.shopp.designsystem.theme.ShoppType
import kotlinx.coroutines.delay

// Checkbox fill/check animations below total ~180ms; give them a beat to
// finish before the row actually leaves the list.
private const val COMPLETE_REMOVE_DELAY_MS = 200L

/**
 * Tap checkbox marks `completing` immediately (drives the
 * visual transition); [onComplete] -- the actual write -- fires after
 * [COMPLETE_REMOVE_DELAY_MS] so the checkbox finishes filling before the row
 * fades out of the list (see [LazyItemScope.animateItem] on the caller's
 * `items` block). Tap elsewhere on the row enters edit mode with the caret
 * placed at the tap position via
 * [androidx.compose.ui.text.TextLayoutResult.getOffsetForPosition].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemRow(
    item: ItemEntity,
    onComplete: () -> Unit,
    onCommitEdit: (title: String, quantity: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    var completing by remember(item.id) { mutableStateOf(false) }
    var editing by remember(item.id) { mutableStateOf(false) }
    var fieldValue by remember(item.id) { mutableStateOf(TextFieldValue(item.title)) }
    var editQuantity by remember(item.id) { mutableStateOf(item.quantity) }
    val displayTitle = titleWithQuantity(item.title, item.quantity)
    var textLayout by remember(item.id) { mutableStateOf<TextLayoutResult?>(null) }
    val focusRequester = remember(item.id) { FocusRequester() }

    LaunchedEffect(completing) {
        if (completing) {
            delay(COMPLETE_REMOVE_DELAY_MS)
            onComplete()
        }
    }

    fun commitIfChanged() {
        val newTitle = fieldValue.text.trim()
        if (newTitle.isEmpty()) return
        if (newTitle != item.title || editQuantity != item.quantity) onCommitEdit(newTitle, editQuantity)
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
                if (!completing) completing = true
            },
        )

        if (editing) {
            var hasBeenFocused by remember(item.id) { mutableStateOf(false) }
            var fieldFocused by remember(item.id) { mutableStateOf(false) }
            val focusManager = LocalFocusManager.current
            val imeVisible by rememberUpdatedState(WindowInsets.isImeVisible)
            // Hiding the keyboard (back gesture or the IME's hide key) doesn't
            // clear Compose focus, so the caret and stepper would linger.
            // Treat a visible to hidden transition as a blur: clearing focus
            // routes it through onFocusChanged below, which commits. Stepper
            // taps never touch focus or the IME, so they don't trigger this.
            LaunchedEffect(item.id) {
                val detector = ImeDismissDetector()
                snapshotFlow { imeVisible }.collect { visible ->
                    if (detector.onImeVisibility(visible) && fieldFocused) {
                        focusManager.clearFocus()
                    }
                }
            }
            BasicTextField(
                value = fieldValue,
                onValueChange = { fieldValue = it },
                textStyle = ShoppType.itemText.copy(color = colors.foreground),
                cursorBrush = SolidColor(colors.accent),
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
                        fieldFocused = state.isFocused
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
            // Sits in the row's 14dp rowGap like the checkbox; its 44dp tap
            // boxes fit the row's existing tapTarget min height.
            QuantityStepper(
                value = editQuantity,
                onValueChange = { editQuantity = it },
                min = MIN_QUANTITY,
                max = MAX_QUANTITY,
            )
        } else {
            LinkifiedText(
                text = displayTitle,
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
                                // The laid-out text may include the " xN"
                                // quantity suffix (e.g. "Milk x2"), which
                                // isn't part of the editable title, so a tap
                                // on it lands the caret at the end of the
                                // title.
                                val index = (textLayout?.getOffsetForPosition(offset) ?: item.title.length)
                                    .coerceAtMost(item.title.length)
                                fieldValue = TextFieldValue(item.title, TextRange(index))
                                editQuantity = item.quantity
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
