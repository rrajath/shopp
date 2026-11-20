package com.rrajath.shopp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.rrajath.shopp.ShoppApplication
import com.rrajath.shopp.capture.CaptureActivity
import com.rrajath.shopp.designsystem.theme.ShoppTheme
import com.rrajath.shopp.domain.LabelRef
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// End to end through the real AppContainer (Room -> ViewModel StateFlow ->
// itemTitleSuggestions -> QuickAddOverlay) for both ways Quick Add opens.
private fun seedHistory() {
    val container = ApplicationProvider.getApplicationContext<ShoppApplication>().container
    runBlocking {
        val added = container.captureItems("banana @costco\nbasil", LabelRef.None)
        added.items.forEach { container.completeItem(it.id) }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InAppQuickAddTitleSuggestionsTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun typingShowsHistorySuggestionsAndPickingOneFillsTheLine() {
        seedHistory()
        val app = ApplicationProvider.getApplicationContext<ShoppApplication>()
        val viewModel = ShoppViewModel(app.container)
        rule.setContent { ShoppTheme { ShoppApp(viewModel = viewModel) } }

        viewModel.openQuickAdd()
        rule.waitForIdle()
        rule.onNode(hasSetTextAction()).performTextInput("ba")
        rule.waitForIdle()

        rule.onNodeWithText("banana").assertIsDisplayed()
        rule.onNodeWithText("basil").assertIsDisplayed()

        rule.onNodeWithText("banana").performClick()
        rule.waitForIdle()
        assertEquals("banana", viewModel.quickAdd.value.draft)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CaptureQuickAddTitleSuggestionsTest {
    @get:Rule val rule = createAndroidComposeRule<CaptureActivity>()

    @Test
    fun typingShowsHistorySuggestions() {
        seedHistory()
        rule.waitForIdle()
        rule.onNode(hasSetTextAction()).performTextInput("bas")
        rule.waitForIdle()

        rule.onNodeWithText("basil").assertIsDisplayed()
    }
}
