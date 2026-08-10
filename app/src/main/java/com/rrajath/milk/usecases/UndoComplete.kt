package com.rrajath.milk.usecases

import androidx.room.withTransaction
import com.rrajath.milk.data.db.ShoppDatabase
import com.rrajath.milk.data.repository.ItemRepository

// Trim also runs here (TDD §4.5): undo can't push anything over the cap, but
// re-running it keeps "at most 100 live completed rows" true unconditionally
// after every state transition, not just after completeItem.
class UndoComplete(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
) {
    suspend operator fun invoke(itemId: String) {
        database.withTransaction {
            val item = itemRepository.getById(itemId) ?: return@withTransaction
            itemRepository.markActive(item)
            itemRepository.trimCompletedBeyond100()
        }
    }
}
