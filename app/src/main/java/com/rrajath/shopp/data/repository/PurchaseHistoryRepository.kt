package com.rrajath.shopp.data.repository

import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.data.db.PurchaseHistoryDao
import com.rrajath.shopp.data.db.PurchaseHistoryEntity
import com.rrajath.shopp.domain.foldForMatching
import kotlinx.coroutines.flow.Flow

class PurchaseHistoryRepository(private val dao: PurchaseHistoryDao) {

    fun observeAll(): Flow<List<PurchaseHistoryEntity>> = dao.observeAll()

    // Called with the item as it was just completed (completedAt set).
    suspend fun recordPurchase(item: ItemEntity) {
        val at = item.completedAt ?: return
        val key = item.title.foldForMatching()
        val existing = dao.get(key)
        dao.upsert(
            PurchaseHistoryEntity(
                titleFolded = key,
                title = item.title,
                labelId = item.labelId,
                count = (existing?.count ?: 0) + 1,
                lastPurchasedAt = at,
                previousPurchasedAt = existing?.lastPurchasedAt,
            ),
        )
    }

    // Reverses recordPurchase for an undone completion. Title and label
    // keep their latest values; the previous ones aren't stored, and the
    // undo window is seconds long, so they almost always match anyway.
    suspend fun undoPurchase(item: ItemEntity) {
        val existing = dao.get(item.title.foldForMatching()) ?: return
        if (existing.count <= 1) {
            dao.delete(existing)
        } else {
            dao.upsert(
                existing.copy(
                    count = existing.count - 1,
                    lastPurchasedAt = existing.previousPurchasedAt ?: existing.lastPurchasedAt,
                    previousPurchasedAt = null,
                ),
            )
        }
    }

    suspend fun reassignLabel(sourceLabelId: String, targetLabelId: String?) {
        dao.reassignLabel(sourceLabelId, targetLabelId)
    }
}
