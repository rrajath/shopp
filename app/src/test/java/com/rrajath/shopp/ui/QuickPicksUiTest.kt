package com.rrajath.shopp.ui

import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.rrajath.shopp.ShoppApplication
import com.rrajath.shopp.designsystem.theme.ShoppTheme
import com.rrajath.shopp.domain.LabelRef
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Through the real AppContainer: completing items fills purchase_history,
// which drives the Frequent/Recent panel in QuickAddOverlay.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuickPicksUiTest {
    @get:Rule val rule = createComposeRule()

    private val app = ApplicationProvider.getApplicationContext<ShoppApplication>()

    // item1 bought 12 times ... item12 once; completed in order, so
    // item12's last purchase is the most recent.
    private fun seed() = runBlocking {
        val c = app.container
        for (n in 1..12) {
            repeat(13 - n) {
                val added = c.captureItems("item$n", LabelRef.None)
                // Debug builds abort an update that doesn't move updatedAt
                // forward, so don't complete within the insert's millisecond.
                Thread.sleep(2)
                added.items.forEach { c.completeItem(it.id) }
            }
        }
    }

    private fun openQuickAdd(): ShoppViewModel {
        val viewModel = ShoppViewModel(app.container)
        rule.setContent { ShoppTheme { ShoppApp(viewModel = viewModel) } }
        viewModel.openQuickAdd()
        rule.waitForIdle()
        return viewModel
    }

    @Test
    fun frequentTabShowsTopTenWithFiveRowsVisible() {
        seed()
        openQuickAdd()

        rule.onNodeWithText("Frequent").assertIsSelected()
        rule.onNodeWithText("item1").assertIsDisplayed()
        // 5 rows of 15sp single-line text + 13dp padding top and bottom.
        rule.onNodeWithTag("quickPicksList").assertHeightIsEqualTo((15.dp + 26.dp) * 5)
        rule.onNodeWithText("item10").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("item11").assertDoesNotExist()
    }

    @Test
    fun recentTabListsMostRecentFirst() {
        seed()
        openQuickAdd()

        rule.onNodeWithText("Recent").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Recent").assertIsSelected()
        rule.onNodeWithText("item12").assertIsDisplayed()
        rule.onNodeWithText("item1").assertDoesNotExist() // 11th most recent
    }

    @Test
    fun tappingAPickAddsItAndHidesItFromTheList() {
        seed()
        openQuickAdd()

        rule.onNodeWithText("item2").performClick()
        rule.waitForIdle()
        val active = runBlocking { app.container.itemRepository.observeActiveItems().first() }
        assertEquals(listOf("item2"), active.map { it.title })
        // Quick Add stays open (default setting) and the pick leaves the
        // list now that it's on the shopping list.
        rule.onNodeWithTag("quickPicksList").assertIsDisplayed()
        rule.onNodeWithText("item11").performScrollTo().assertIsDisplayed() // backfills the slot item2 left
    }

    @Test
    fun typingSwapsTheListForSuggestions() {
        seed()
        val viewModel = openQuickAdd()

        rule.onNode(hasSetTextAction()).performTextInput("item1")
        rule.waitForIdle()
        rule.onNodeWithTag("quickPicksList").assertDoesNotExist()
        rule.onNodeWithText("Frequent").assertDoesNotExist()
        rule.onNodeWithText("item11").assertIsDisplayed() // title suggestion
        assertTrue(viewModel.quickAdd.value.open)
    }

    @Test
    fun hiddenWithNoPurchaseHistory() {
        openQuickAdd()
        rule.onNodeWithText("Frequent").assertDoesNotExist()
    }
}
