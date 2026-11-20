package com.rrajath.shopp.ui

import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.data.db.PurchaseHistoryEntity
import com.rrajath.shopp.data.db.STATE_ACTIVE
import com.rrajath.shopp.domain.foldForMatching
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickPicksTest {

    private fun history(title: String, count: Int, at: Long, labelId: String? = null) = PurchaseHistoryEntity(
        titleFolded = title.foldForMatching(),
        title = title,
        labelId = labelId,
        count = count,
        lastPurchasedAt = at,
        previousPurchasedAt = null,
    )

    private fun active(title: String) = ItemEntity(
        id = title, title = title, labelId = null, state = STATE_ACTIVE,
        createdAt = 0, updatedAt = 0, completedAt = null, deletedAt = null,
    )

    private fun label(id: String) = LabelEntity(
        id = id, name = id, nameFolded = id, colorIndex = 0,
        createdAt = 0, updatedAt = 0, lastUsedAt = 0, deletedAt = null,
    )

    @Test
    fun frequentOrdersByCountThenRecency() {
        val h = listOf(history("eggs", 2, 300), history("milk", 5, 100), history("bread", 2, 200))
        assertEquals(listOf("milk", "eggs", "bread"), quickPicks(h, emptyList(), emptyList()).frequent.map { it.title })
    }

    @Test
    fun recentOrdersByLastPurchase() {
        val h = listOf(history("milk", 5, 100), history("eggs", 1, 300), history("bread", 2, 200))
        assertEquals(listOf("eggs", "bread", "milk"), quickPicks(h, emptyList(), emptyList()).recent.map { it.title })
    }

    @Test
    fun capsAtTenAndBackfillsPastItemsAlreadyOnTheList() {
        val h = (1..12).map { history("item$it", count = 20 - it, at = 100L - it) }
        val picks = quickPicks(h, listOf(active("ITEM1"), active("item2")), emptyList())
        assertEquals((3..12).map { "item$it" }, picks.frequent.map { it.title })
        assertEquals((3..12).map { "item$it" }, picks.recent.map { it.title })
    }

    @Test
    fun deadLabelReadsAsInbox() {
        val h = listOf(history("milk", 1, 1, labelId = "gone"), history("eggs", 1, 2, labelId = "dairy"))
        val picks = quickPicks(h, emptyList(), listOf(label("dairy")))
        assertEquals("dairy", picks.recent.first { it.title == "eggs" }.labelId)
        assertNull(picks.recent.first { it.title == "milk" }.labelId)
    }

    @Test
    fun emptyWhenEverythingIsOnTheList() {
        assertTrue(quickPicks(listOf(history("milk", 1, 1)), listOf(active("milk")), emptyList()).isEmpty)
    }
}
