package com.rrajath.shopp.usecases

import androidx.room.withTransaction
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.ItemRepository
import com.rrajath.shopp.data.repository.LabelRepository

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
