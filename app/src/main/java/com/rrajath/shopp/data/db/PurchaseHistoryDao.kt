package com.rrajath.shopp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseHistoryDao {

    // The whole table: one row per distinct title, so it stays small, and
    // Frequent/Recent are both derived from it (after hiding titles already
    // on the list) in quickPicks().
    @Query("SELECT * FROM purchase_history ORDER BY lastPurchasedAt DESC")
    fun observeAll(): Flow<List<PurchaseHistoryEntity>>

    @Query("SELECT * FROM purchase_history WHERE titleFolded = :titleFolded")
    suspend fun get(titleFolded: String): PurchaseHistoryEntity?

    @Upsert
    suspend fun upsert(entry: PurchaseHistoryEntity)

    @Delete
    suspend fun delete(entry: PurchaseHistoryEntity)

    @Query("UPDATE purchase_history SET labelId = :targetLabelId WHERE labelId = :sourceLabelId")
    suspend fun reassignLabel(sourceLabelId: String, targetLabelId: String?)
}
