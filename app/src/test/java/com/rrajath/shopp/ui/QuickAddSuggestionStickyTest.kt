package com.rrajath.shopp.ui

import androidx.test.core.app.ApplicationProvider
import com.rrajath.shopp.AppContainer
import com.rrajath.shopp.ShoppApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Picking a title suggestion moves the sticky chip to the suggestion's
// label, then puts the earlier chip back once the item is added (unless the
// user picked a chip by hand or typed an `@token` meanwhile).
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuickAddSuggestionStickyTest {

    private lateinit var container: AppContainer
    private lateinit var costco: String
    private lateinit var pcc: String
    private lateinit var safeway: String

    @Before
    fun setUp() {
        container = ApplicationProvider.getApplicationContext<ShoppApplication>().container
        runBlocking {
            costco = container.labelRepository.resolveOrCreate("costco").id
            pcc = container.labelRepository.resolveOrCreate("pcc").id
            safeway = container.labelRepository.resolveOrCreate("safeway").id
        }
    }

    // Runs body with a controller bound to this runBlocking scope, so
    // submitAndWait can join the coroutine submit() launches.
    private fun withController(body: suspend CoroutineScope.(QuickAddController) -> Unit) = runBlocking {
        val controller = QuickAddController(this, container, initiallyOpen = true)
        body(controller)
    }

    private suspend fun CoroutineScope.submitAndWait(controller: QuickAddController) {
        controller.submit()
        coroutineContext.job.children.toList().forEach { it.join() }
    }

    private suspend fun labelOf(title: String): String? =
        container.itemRepository.getActiveItems().first { it.title == title }.labelId

    @Test
    fun pickingSuggestionSelectsItsLabelChip() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("banana", costco))
        assertEquals("banana", c.state.value.draft)
        assertEquals(costco, c.state.value.stickyLabelId)
    }

    @Test
    fun pickingInboxSuggestionSelectsInboxChip() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("banana", null))
        assertNull(c.state.value.stickyLabelId)
    }

    @Test
    fun addRestoresChipFromBeforeThePick() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("banana", costco))
        submitAndWait(c)

        assertEquals(costco, labelOf("banana"))
        assertEquals(pcc, c.state.value.stickyLabelId)
        assertNull(c.state.value.stickyBeforeSuggestion)
    }

    @Test
    fun addRestoresInboxWhenInboxWasSelectedBeforeThePick() = withController { c ->
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("banana", costco))
        submitAndWait(c)

        assertEquals(costco, labelOf("banana"))
        assertNull(c.state.value.stickyLabelId)
    }

    @Test
    fun secondPickKeepsTheOriginalSavedChip() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("banana", costco))
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("basil", safeway))
        assertEquals(safeway, c.state.value.stickyLabelId)

        submitAndWait(c)
        assertEquals(safeway, labelOf("basil"))
        assertEquals(pcc, c.state.value.stickyLabelId)
    }

    @Test
    fun manualChipAfterPickWinsAndStaysAfterAdd() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("banana", costco))
        c.selectStickyChip(safeway)
        assertNull(c.state.value.stickyBeforeSuggestion)

        submitAndWait(c)
        assertEquals(safeway, labelOf("banana"))
        assertEquals(safeway, c.state.value.stickyLabelId)
    }

    @Test
    fun clearingDraftRestoresChipImmediately() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("banana", costco))
        c.updateDraft("")

        assertEquals(pcc, c.state.value.stickyLabelId)
        assertNull(c.state.value.stickyBeforeSuggestion)
        assertEquals(emptyMap<String, String?>(), c.state.value.suggestionLabels)
    }

    @Test
    fun editingTextAfterPickKeepsSuggestionLabel() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("banana", costco))
        c.updateDraft("banana bread")

        assertEquals(costco, c.state.value.stickyLabelId)
        submitAndWait(c)
        // The edited title no longer matches the override, so it lands
        // under the visible chip, which is still the suggestion's label.
        assertEquals(costco, labelOf("banana bread"))
        assertEquals(pcc, c.state.value.stickyLabelId)
    }

    @Test
    fun clearingDraftWithoutPickLeavesChipAlone() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("milk")
        c.updateDraft("")
        assertEquals(pcc, c.state.value.stickyLabelId)
    }

    @Test
    fun closeDropsSavedChip() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("banana", costco))
        c.close()
        c.open()

        assertNull(c.state.value.stickyBeforeSuggestion)
        assertNull(c.state.value.stickyLabelId)
    }

    @Test
    fun labelTokenInSubmitWinsOverRestore() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("ba")
        c.acceptTitleSuggestion(TitleSuggestion("banana", costco))
        c.updateDraft("banana\neggs @safeway")
        submitAndWait(c)

        assertEquals(costco, labelOf("banana"))
        assertEquals(safeway, labelOf("eggs"))
        assertEquals(safeway, c.state.value.stickyLabelId)
    }

    @Test
    fun plainStickyFlowWithoutPickIsUnchanged() = withController { c ->
        c.selectStickyChip(pcc)
        c.updateDraft("milk")
        submitAndWait(c)
        assertEquals(pcc, labelOf("milk"))
        assertEquals(pcc, c.state.value.stickyLabelId)

        c.updateDraft("eggs @costco")
        submitAndWait(c)
        assertEquals(costco, c.state.value.stickyLabelId)
    }
}
