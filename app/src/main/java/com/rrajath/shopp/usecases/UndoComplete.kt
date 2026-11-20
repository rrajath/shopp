package com.rrajath.shopp.usecases

import androidx.room.withTransaction
import com.rrajath.shopp.data.db.STATE_COMPLETED
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.ItemRepository
import com.rrajath.shopp.data.repository.PurchaseHistoryRepository

// Trim also runs here: undo can't push anything over the cap, but
// re-running it keeps "at most 100 live completed rows" true unconditionally
// after every state transition, not just after completeItem. An undone
// completion was a mis-tap, not a purchase, so its count is taken back.
class UndoComplete(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
    private val purchaseHistoryRepository: PurchaseHistoryRepository,
) {
    suspend operator fun invoke(itemId: String) {
        database.withTransaction {
            val item = itemRepository.getById(itemId) ?: return@withTransaction
            if (item.state == STATE_COMPLETED) purchaseHistoryRepository.undoPurchase(item)
            itemRepository.markActive(item)
            itemRepository.trimCompletedBeyond100()
        }
    }
}
