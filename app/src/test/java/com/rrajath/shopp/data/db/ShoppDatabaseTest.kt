package com.rrajath.shopp.data.db

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShoppDatabaseTest {

    private lateinit var db: ShoppDatabase

    @Before
    fun setUp() {
        db = ShoppDatabase.buildInMemory(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `schema creates and basic round trip works`() = runBlocking {
        val now = 1_000L
        val label = LabelEntity(
            id = "l1", name = "costco", nameFolded = "costco", colorIndex = 0,
            createdAt = now, updatedAt = now, lastUsedAt = now, deletedAt = null,
        )
        db.labelDao().insert(label)

        val item = ItemEntity(
            id = "i1", title = "milk", labelId = "l1", state = STATE_ACTIVE,
            createdAt = now, updatedAt = now, completedAt = null, deletedAt = null,
        )
        db.itemDao().insertAll(listOf(item))

        val active = db.itemDao().observeActiveItems().first()
        assertEquals(1, active.size)
    }

    @Test
    fun `partial unique index blocks duplicate live folded names`() = runBlocking {
        val now = 1_000L
        db.labelDao().insert(
            LabelEntity("l1", "costco", "costco", 0, now, now, now, deletedAt = null)
        )
        assertThrows(Exception::class.java) {
            runBlocking {
                db.labelDao().insert(
                    LabelEntity("l2", "Costco", "costco", 1, now, now, now, deletedAt = null)
                )
            }
        }
        Unit
    }

    @Test
    fun `partial unique index allows folded name reuse after tombstone`() = runBlocking {
        val now = 1_000L
        db.labelDao().insert(
            LabelEntity("l1", "costco", "costco", 0, now, now, now, deletedAt = null)
        )
        db.labelDao().tombstone("l1", now + 1)

        // Should not throw: the first "costco" is now tombstoned.
        db.labelDao().insert(
            LabelEntity("l2", "costco", "costco", 1, now + 2, now + 2, now + 2, deletedAt = null)
        )
        assertEquals("l2", db.labelDao().findByFoldedName("costco")?.id)
    }

    @Test
    fun `debug trigger aborts update that does not bump updatedAt`() = runBlocking {
        val now = 1_000L
        val item = ItemEntity(
            id = "i1", title = "milk", labelId = null, state = STATE_ACTIVE,
            createdAt = now, updatedAt = now, completedAt = null, deletedAt = null,
        )
        db.itemDao().insertAll(listOf(item))

        assertThrows(Exception::class.java) {
            runBlocking {
                // updatedAt unchanged -> the debug guard trigger should abort this.
                db.itemDao().update(item.copy(title = "milk 2L"))
            }
        }
        Unit
    }

    @Test
    fun `trim keeps at most 100 live completed items, hard-deleting the rest`() = runBlocking {
        val base = 1_000L
        val items = (1..105).map { i ->
            ItemEntity(
                id = "i$i", title = "item $i", labelId = null, state = STATE_COMPLETED,
                createdAt = base, updatedAt = base, completedAt = base + i, deletedAt = null,
            )
        }
        db.itemDao().insertAll(items)
        db.itemDao().trimCompletedBeyond100()

        val remaining = db.itemDao().observeCompletedItems().first()
        assertEquals(100, remaining.size)
        // The 5 oldest completions (i1..i5) should be the ones trimmed.
        assertEquals("i105", remaining.first().id)
        assertNull(remaining.find { it.id == "i1" })
        // Hard-deleted, not tombstoned -- the row shouldn't exist at all.
        assertNull(db.itemDao().getById("i1"))
    }
}
