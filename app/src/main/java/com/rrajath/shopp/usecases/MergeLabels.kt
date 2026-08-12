package com.rrajath.shopp.usecases

import androidx.room.withTransaction
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.ItemRepository
import com.rrajath.shopp.data.repository.LabelRepository

// Reassign then tombstone, one transaction (TDD §4.3). This shape -- never a
// rename -- is what V2 sync requires and costs nothing to get right now.
class MergeLabels(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(sourceLabelId: String, targetLabelId: String) {
        database.withTransaction {
            itemRepository.reassignLabel(sourceLabelId, targetLabelId)
            labelRepository.tombstone(sourceLabelId)
        }
    }
}
