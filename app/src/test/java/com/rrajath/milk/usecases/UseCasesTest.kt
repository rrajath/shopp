package com.rrajath.milk.usecases

import androidx.test.core.app.ApplicationProvider
import com.rrajath.milk.data.db.ItemEntity
import com.rrajath.milk.data.db.STATE_COMPLETED
import com.rrajath.milk.data.db.ShoppDatabase
import com.rrajath.milk.data.repository.ItemRepository
import com.rrajath.milk.data.repository.LabelRepository
import com.rrajath.milk.domain.Clock
import com.rrajath.milk.domain.LabelRef
import com.rrajath.milk.domain.Uuidv7IdGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class FakeClock(private var t: Long = 1_000L) : Clock {
    override fun nowMillis(): Long = t
    fun advance(ms: Long = 1): Long {
        t += ms
        return t
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UseCasesTest {

    private lateinit var db: ShoppDatabase
    private lateinit var clock: FakeClock
    private lateinit var itemRepository: ItemRepository
    private lateinit var labelRepository: LabelRepository

    private lateinit var captureItems: CaptureItems
    private lateinit var completeItem: CompleteItem
    private lateinit var undoComplete: UndoComplete
    private lateinit var editTitle: EditTitle
    private lateinit var renameLabel: RenameLabel
    private lateinit var mergeLabels: MergeLabels
    private lateinit var deleteLabel: DeleteLabel
    private lateinit var readdCompleted: ReaddCompleted

    @Before
    fun setUp() {
        db = ShoppDatabase.buildInMemory(ApplicationProvider.getApplicationContext())
        clock = FakeClock()
        val idGenerator = Uuidv7IdGenerator
        itemRepository = ItemRepository(db.itemDao(), clock)
        labelRepository = LabelRepository(db.labelDao(), clock, idGenerator)

        captureItems = CaptureItems(db, itemRepository, labelRepository, clock, idGenerator)
        completeItem = CompleteItem(db, itemRepository)
        undoComplete = UndoComplete(db, itemRepository)
        editTitle = EditTitle(db, itemRepository, labelRepository)
        renameLabel = RenameLabel(db, labelRepository)
        mergeLabels = MergeLabels(db, itemRepository, labelRepository)
        deleteLabel = DeleteLabel(db, itemRepository, labelRepository)
        readdCompleted = ReaddCompleted(db, itemRepository, labelRepository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `capture creates items in paste order and updates sticky`() = runBlocking {
        val result = captureItems("carrots\ntomatoes\nonions", LabelRef.None)
        assertEquals(3, result.items.size)
        val active = itemRepository.observeActiveItems().first()
        assertEquals(listOf("carrots", "tomatoes", "onions"), active.map { it.title })
    }

    @Test
    fun `capture resolves a token to a real label and sets sticky`() = runBlocking {
        val result = captureItems("milk @costco\neggs", LabelRef.None)
        val active = itemRepository.observeActiveItems().first()
        val costco = labelRepository.observeLabels().first().first { it.name == "costco" }
        assertTrue(active.all { it.labelId == costco.id })
        assertEquals(LabelRef.Id(costco.id), result.newSticky)
    }

    @Test
    fun `completing an item trims completed beyond 100 in the same transaction`() = runBlocking {
        val seeded = (1..100).map { i ->
            ItemEntity(
                id = "seed$i", title = "seed $i", labelId = null, state = STATE_COMPLETED,
                createdAt = clock.nowMillis(), updatedAt = clock.nowMillis(),
                completedAt = clock.advance(), deletedAt = null,
            )
        }
        itemRepository.insertAll(seeded)
        clock.advance(100)
        captureItems("fresh milk", LabelRef.None)
        val toComplete = itemRepository.observeActiveItems().first().first { it.title == "fresh milk" }

        clock.advance(100)
        completeItem(toComplete.id)

        val completed = itemRepository.observeCompletedItems().first()
        assertEquals(100, completed.size)
        assertTrue(completed.any { it.title == "fresh milk" }) // newest completion survives the trim
        assertNull(completed.find { it.id == "seed1" })
    }

    @Test
    fun `undo restores original position via created_at`() = runBlocking {
        captureItems("carrots\ntomatoes\nonions", LabelRef.None)
        val tomatoes = itemRepository.observeActiveItems().first().first { it.title == "tomatoes" }

        clock.advance(500)
        completeItem(tomatoes.id)
        clock.advance(500)
        undoComplete(tomatoes.id)

        val after = itemRepository.observeActiveItems().first()
        assertEquals(listOf("carrots", "tomatoes", "onions"), after.map { it.title })
    }

    @Test
    fun `edit title without a token preserves the existing label`() = runBlocking {
        captureItems("milk @costco", LabelRef.None)
        val item = itemRepository.observeActiveItems().first().first()
        val originalLabelId = item.labelId

        clock.advance(100)
        editTitle(item.id, "milk 2L")

        val updated = itemRepository.getById(item.id)!!
        assertEquals("milk 2L", updated.title)
        assertEquals(originalLabelId, updated.labelId)
    }

    @Test
    fun `edit title with a new token re-tags the item`() = runBlocking {
        captureItems("milk", LabelRef.None)
        val item = itemRepository.observeActiveItems().first().first()

        clock.advance(100)
        editTitle(item.id, "milk @qfc")

        val updated = itemRepository.getById(item.id)!!
        assertEquals("milk", updated.title)
        val qfc = labelRepository.observeLabels().first().first { it.name == "qfc" }
        assertEquals(qfc.id, updated.labelId)
    }

    @Test
    fun `rename rejects a folded collision with a different live label`() = runBlocking {
        captureItems("a @costco\nb @qfc", LabelRef.None)
        val qfc = labelRepository.observeLabels().first().first { it.name == "qfc" }

        clock.advance(100)
        val result = renameLabel(qfc.id, "Costco")

        assertEquals(RenameLabel.Result.NameTaken, result)
        assertEquals("qfc", labelRepository.getById(qfc.id)!!.name)
    }

    @Test
    fun `merge reassigns items then tombstones the source in one transaction`() = runBlocking {
        captureItems("a @costco\nb @costco\nc @qfc", LabelRef.None)
        val costco = labelRepository.observeLabels().first().first { it.name == "costco" }
        val qfc = labelRepository.observeLabels().first().first { it.name == "qfc" }

        clock.advance(100)
        mergeLabels(costco.id, qfc.id)

        val active = itemRepository.observeActiveItems().first()
        assertTrue(active.all { it.labelId == qfc.id })
        assertNotNull(labelRepository.getById(costco.id)!!.deletedAt) // tombstoned, not gone
    }

    @Test
    fun `delete label moves items to Inbox and tombstones the label`() = runBlocking {
        captureItems("a @costco\nb @costco", LabelRef.None)
        val costco = labelRepository.observeLabels().first().first { it.name == "costco" }

        clock.advance(100)
        deleteLabel(costco.id)

        val active = itemRepository.observeActiveItems().first()
        assertTrue(active.all { it.labelId == null })
        assertNotNull(labelRepository.getById(costco.id)!!.deletedAt)
    }

    @Test
    fun `readd completed flips the same row back to active, removing it from Recently Completed`() = runBlocking {
        captureItems("milk @costco", LabelRef.None)
        val item = itemRepository.observeActiveItems().first().first()
        clock.advance(100)
        completeItem(item.id)

        clock.advance(100)
        readdCompleted(item.id)

        val active = itemRepository.observeActiveItems().first()
        assertEquals(1, active.size)
        assertEquals("milk", active.first().title)
        assertNotNull(active.first().labelId)
        assertEquals(item.id, active.first().id)

        val completed = itemRepository.observeCompletedItems().first()
        assertEquals(0, completed.size)
    }

    @Test
    fun `every mutating use case bumps updatedAt`() = runBlocking {
        captureItems("milk @costco", LabelRef.None)
        val item = itemRepository.observeActiveItems().first().first()
        val label = labelRepository.getById(item.labelId!!)!!

        clock.advance(100)
        completeItem(item.id)
        val afterComplete = itemRepository.getById(item.id)!!
        assertTrue(afterComplete.updatedAt > item.updatedAt)

        clock.advance(100)
        undoComplete(item.id)
        val afterUndo = itemRepository.getById(item.id)!!
        assertTrue(afterUndo.updatedAt > afterComplete.updatedAt)

        clock.advance(100)
        editTitle(item.id, "milk 2L")
        val afterEdit = itemRepository.getById(item.id)!!
        assertTrue(afterEdit.updatedAt > afterUndo.updatedAt)

        clock.advance(100)
        renameLabel(label.id, "Costco Wholesale")
        val afterRename = labelRepository.getById(label.id)!!
        assertTrue(afterRename.updatedAt > label.updatedAt)
    }
}
