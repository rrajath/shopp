package com.rrajath.shopp.usecases

import androidx.room.withTransaction
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.ItemRepository

// The write is immediate, not deferred behind the undo window (TDD §6.3) --
// trim runs in the same transaction so "at most 100 live completed rows"
// holds after every completion.
class CompleteItem(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
) {
    suspend operator fun invoke(itemId: String) {
        database.withTransaction {
            val item = itemRepository.getById(itemId) ?: return@withTransaction
            itemRepository.markCompleted(item)
            itemRepository.trimCompletedBeyond100()
        }
    }
}
