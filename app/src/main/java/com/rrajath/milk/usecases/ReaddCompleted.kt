package com.rrajath.milk.usecases

import androidx.room.withTransaction
import com.rrajath.milk.data.db.STATE_ACTIVE
import com.rrajath.milk.data.db.ShoppDatabase
import com.rrajath.milk.data.repository.ItemRepository
import com.rrajath.milk.data.repository.LabelRepository
import com.rrajath.milk.domain.Clock
import com.rrajath.milk.domain.IdGenerator

// Inserts a *new* item (new id, new created_at) with the same title/label;
// the completed row is untouched (PRD §9 -- "the entry remains in Recently
// Completed").
class ReaddCompleted(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
    private val labelRepository: LabelRepository,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) {
    suspend operator fun invoke(completedItemId: String) {
        database.withTransaction {
            val completed = itemRepository.getById(completedItemId) ?: return@withTransaction
            val now = clock.nowMillis()
            itemRepository.insertAll(
                listOf(
                    completed.copy(
                        id = idGenerator.newId(),
                        state = STATE_ACTIVE,
                        createdAt = now,
                        updatedAt = now,
                        completedAt = null,
                        deletedAt = null,
                    )
                )
            )
            completed.labelId?.let { labelRepository.touch(it) }
        }
    }
}
