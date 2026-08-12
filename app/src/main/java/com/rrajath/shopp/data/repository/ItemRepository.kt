package com.rrajath.shopp.data.repository

import com.rrajath.shopp.data.db.ItemDao
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.data.db.STATE_ACTIVE
import com.rrajath.shopp.data.db.STATE_COMPLETED
import com.rrajath.shopp.domain.Clock
import kotlinx.coroutines.flow.Flow

// The single place that stamps updatedAt on writes to existing rows (TDD
// §3.5) — use cases never touch the DAO for updates directly.
class ItemRepository(
    private val itemDao: ItemDao,
    private val clock: Clock,
) {
    fun observeActiveItems(): Flow<List<ItemEntity>> = itemDao.observeActiveItems()

    fun observeCompletedItems(): Flow<List<ItemEntity>> = itemDao.observeCompletedItems()

    suspend fun getById(id: String): ItemEntity? = itemDao.getById(id)

    suspend fun insertAll(items: List<ItemEntity>) = itemDao.insertAll(items)

    suspend fun markCompleted(item: ItemEntity) {
        val now = clock.nowMillis()
        itemDao.update(item.copy(state = STATE_COMPLETED, completedAt = now, updatedAt = now))
    }

    suspend fun markActive(item: ItemEntity) {
        val now = clock.nowMillis()
        itemDao.update(item.copy(state = STATE_ACTIVE, completedAt = null, updatedAt = now))
    }

    suspend fun updateTitleAndLabel(item: ItemEntity, title: String, labelId: String?) {
        val now = clock.nowMillis()
        itemDao.update(item.copy(title = title, labelId = labelId, updatedAt = now))
    }

    suspend fun reassignLabel(sourceLabelId: String, targetLabelId: String?) {
        itemDao.reassignLabel(sourceLabelId, targetLabelId, clock.nowMillis())
    }

    suspend fun trimCompletedBeyond100() {
        itemDao.trimCompletedBeyond100()
    }

    suspend fun tombstoneAllCompleted() {
        itemDao.tombstoneAllCompleted(clock.nowMillis())
    }
}
