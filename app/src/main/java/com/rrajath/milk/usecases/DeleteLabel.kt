package com.rrajath.milk.usecases

import androidx.room.withTransaction
import com.rrajath.milk.data.db.ShoppDatabase
import com.rrajath.milk.data.repository.ItemRepository
import com.rrajath.milk.data.repository.LabelRepository

// Items survive and move to Inbox; only the label is tombstoned (TDD §4.3).
class DeleteLabel(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(labelId: String) {
        database.withTransaction {
            itemRepository.reassignLabel(labelId, null)
            labelRepository.tombstone(labelId)
        }
    }
}
