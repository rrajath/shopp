package com.rrajath.milk.usecases

import androidx.room.withTransaction
import com.rrajath.milk.data.db.ShoppDatabase
import com.rrajath.milk.data.repository.ItemRepository
import com.rrajath.milk.data.repository.LabelRepository

// Flips the *same* row back to active (same id, same createdAt) -- the item
// leaves Recently Completed and reappears in its original group, per the
// user-requested behavior change (superseding the old "leave the completed
// row untouched, insert a duplicate" PRD §9 behavior).
class ReaddCompleted(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(completedItemId: String) {
        database.withTransaction {
            val completed = itemRepository.getById(completedItemId) ?: return@withTransaction
            itemRepository.markActive(completed)
            completed.labelId?.let { labelRepository.touch(it) }
        }
    }
}
