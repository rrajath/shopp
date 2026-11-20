package com.rrajath.shopp.usecases

import androidx.room.withTransaction
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.ItemRepository
import com.rrajath.shopp.data.repository.PurchaseHistoryRepository

// The write is immediate, not deferred behind the undo window --
// trim runs in the same transaction so "at most 100 live completed rows"
// holds after every completion. The purchase is counted here too (for
// Quick Add's Frequent/Recent); UndoComplete takes it back.
// recordPurchase = false is for undoing a Recently Completed re-add, which
// puts back a completion that was already counted.
class CompleteItem(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
    private val purchaseHistoryRepository: PurchaseHistoryRepository,
) {
    suspend operator fun invoke(itemId: String, recordPurchase: Boolean = true) {
        database.withTransaction {
            val item = itemRepository.getById(itemId) ?: return@withTransaction
            val completed = itemRepository.markCompleted(item)
            if (recordPurchase) purchaseHistoryRepository.recordPurchase(completed)
            itemRepository.trimCompletedBeyond100()
        }
    }
}
