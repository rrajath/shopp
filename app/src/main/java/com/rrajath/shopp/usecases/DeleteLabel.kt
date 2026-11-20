package com.rrajath.shopp.usecases

import androidx.room.withTransaction
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.ItemRepository
import com.rrajath.shopp.data.repository.LabelRepository
import com.rrajath.shopp.data.repository.PurchaseHistoryRepository

// Items survive and move to Inbox; only the label is tombstoned. Purchase
// history follows the items, so a quick pick re-adds to Inbox too.
class DeleteLabel(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
    private val labelRepository: LabelRepository,
    private val purchaseHistoryRepository: PurchaseHistoryRepository,
) {
    suspend operator fun invoke(labelId: String) {
        database.withTransaction {
            itemRepository.reassignLabel(labelId, null)
            purchaseHistoryRepository.reassignLabel(labelId, null)
            labelRepository.tombstone(labelId)
        }
    }
}
