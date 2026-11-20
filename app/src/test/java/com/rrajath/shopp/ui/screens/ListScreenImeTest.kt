package com.rrajath.shopp.ui.screens

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.designsystem.theme.ShoppDimens
import com.rrajath.shopp.designsystem.theme.ShoppTheme
import com.rrajath.shopp.ui.ListSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Regression: with edge to edge the window doesn't resize for the keyboard,
// so an inline edit on a row in the bottom half of the list used to sit
// behind the IME. Insets are dispatched to the ComposeView by hand, the same
// path the platform uses.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ListScreenImeTest {
    @get:Rule val rule = createComposeRule()

    private lateinit var view: View
    private val commits = mutableListOf<Pair<String, String>>()

    private val items = (0 until 30).map { i ->
        ItemEntity(
            id = "id-$i",
            title = "Item $i",
            labelId = null,
            state = "active",
            createdAt = i.toLong(),
            updatedAt = i.toLong(),
            completedAt = null,
            deletedAt = null,
        )
    }

    private fun setUpList() {
        rule.setContent {
            view = LocalView.current
            ShoppTheme {
                Box(Modifier.fillMaxSize().testTag("root")) {
                    ListScreen(
                        sections = listOf(ListSection(labelId = null, name = "All items", colorIndex = null, items = items)),
                        undo = null,
                        emptyTitle = "",
                        emptyBody = "",
                        onCompleteItem = {},
                        onCommitEdit = { id, title, _ -> commits += id to title },
                        onUndo = {},
                        onAddClick = {},
                        onDrawerDrag = {},
                        onDrawerDragEnd = {},
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    private fun dispatchIme(bottomPx: Int) {
        val insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, bottomPx))
            .setVisible(WindowInsetsCompat.Type.ime(), bottomPx > 0)
            .build()
        rule.runOnIdle { ViewCompat.dispatchApplyWindowInsets(view, insets) }
        rule.waitForIdle()
    }

    /** Taps the lowest fully visible row in the bottom half; returns its index. */
    private fun startEditingBottomRow(rootHeight: Float): Int {
        val index = items.indices.last { i ->
            val node = rule.onNodeWithText("Item $i").runCatching { fetchSemanticsNode() }.getOrNull()
            node != null && node.boundsInRoot.bottom <= rootHeight && node.boundsInRoot.top > rootHeight / 2
        }
        rule.onNodeWithText("Item $index").performClick()
        rule.waitForIdle()
        return index
    }

    @Test
    fun editingRowInBottomHalfIsScrolledAboveKeyboardAndFabClearance() {
        setUpList()
        val rootHeight = rule.onNodeWithTag("root").fetchSemanticsNode().boundsInRoot.height
        startEditingBottomRow(rootHeight)

        val imePx = (rootHeight / 2).toInt()
        dispatchIme(imePx)

        val field = rule.onNode(hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        val clearancePx = with(rule.density) { ShoppDimens.listBottomPadding.toPx() }
        val visibleBottom = rootHeight - imePx
        assertTrue(
            "field bottom ${field.bottom} should sit above ${visibleBottom - clearancePx}",
            field.bottom <= visibleBottom - clearancePx + 1f,
        )
        assertTrue("field top ${field.top} should be on screen", field.top >= 0f)
    }

    @Test
    fun hidingKeyboardStillCommitsTheEdit() {
        setUpList()
        val rootHeight = rule.onNodeWithTag("root").fetchSemanticsNode().boundsInRoot.height
        val index = startEditingBottomRow(rootHeight)
        dispatchIme((rootHeight / 2).toInt())

        rule.onNode(hasSetTextAction()).performTextReplacement("Edited")
        rule.waitForIdle()
        dispatchIme(0)

        assertEquals(listOf("id-$index" to "Edited"), commits)
        assertFalse(
            "row should leave edit mode",
            rule.onNode(hasSetTextAction()).runCatching { fetchSemanticsNode() }.isSuccess,
        )
    }
}
