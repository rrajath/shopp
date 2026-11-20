package com.rrajath.shopp.ui

import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.data.db.STATE_COMPLETED
import org.junit.Assert.assertEquals
import org.junit.Test

class ItemTitleSuggestionsTest {

    private var nextId = 0

    // Callers pass completedItems most-recent first (ItemDao orders by
    // completedAt DESC), so list order here stands in for recency.
    private fun completed(title: String, labelId: String? = null) = ItemEntity(
        id = "id${nextId++}",
        title = title,
        labelId = labelId,
        state = STATE_COMPLETED,
        createdAt = 0,
        updatedAt = 0,
        completedAt = 0,
        deletedAt = null,
    )

    private fun titles(draft: String, items: List<ItemEntity>) = itemTitleSuggestions(draft, items).map { it.title }

    @Test
    fun prefixMatchFromFirstCharacter() {
        val history = listOf(completed("banana"), completed("avocado oil"), completed("onion"))
        assertEquals(listOf("banana"), titles("b", history))
        assertEquals(listOf("avocado oil"), titles("avo", history))
    }

    @Test
    fun matchIsCaseInsensitiveAndIgnoresSurroundingWhitespace() {
        val history = listOf(completed("Milk"))
        assertEquals(listOf("Milk"), titles("mI", history))
        assertEquals(listOf("Milk"), titles("  mi ", history))
    }

    @Test
    fun prefixOnlyNotContains() {
        assertEquals(emptyList<String>(), titles("oil", listOf(completed("avocado oil"))))
    }

    @Test
    fun exactMatchIsSkipped() {
        assertEquals(emptyList<String>(), titles("milk", listOf(completed("Milk"))))
        assertEquals(listOf("Milk chocolate"), titles("milk", listOf(completed("Milk"), completed("Milk chocolate"))))
    }

    @Test
    fun emptyOrBlankLineYieldsNothing() {
        val history = listOf(completed("Milk"))
        assertEquals(emptyList<String>(), titles("", history))
        assertEquals(emptyList<String>(), titles("   ", history))
        assertEquals(emptyList<String>(), titles("bread\n", history))
    }

    @Test
    fun emptyHistoryYieldsNothing() {
        assertEquals(emptyList<String>(), titles("mi", emptyList()))
    }

    @Test
    fun midLabelTokenDefersToLabelPopover() {
        val history = listOf(completed("Milk"))
        assertEquals(emptyList<String>(), titles("mi @", history))
        assertEquals(emptyList<String>(), titles("mi @gro", history))
    }

    @Test
    fun matchesOnlyTheLineBeingTyped() {
        val history = listOf(completed("Milk"), completed("Bread"))
        assertEquals(listOf("Bread"), titles("milk\nbr", history))
    }

    @Test
    fun dedupesFoldedTitlesKeepingMostRecentLabel() {
        val history = listOf(
            completed("Milk", labelId = "costco"),
            completed("milk", labelId = "pcc"),
            completed("Milo", labelId = null),
        )
        val result = itemTitleSuggestions("mi", history)
        assertEquals(listOf(TitleSuggestion("Milk", "costco"), TitleSuggestion("Milo", null)), result)
    }

    @Test
    fun capsAtFiveSuggestions() {
        val history = (1..8).map { completed("item $it") }
        assertEquals((1..5).map { "item $it" }, titles("item", history))
    }
}
