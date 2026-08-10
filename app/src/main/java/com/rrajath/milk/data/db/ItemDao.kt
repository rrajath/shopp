package com.rrajath.milk.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    // Inbox items first, then labelled items; created_at/id give a total
    // order within each group (see Uuidv7's monotonic counter).
    @Query(
        "SELECT * FROM items WHERE deletedAt IS NULL AND state = 'active' " +
            "ORDER BY (labelId IS NOT NULL), createdAt ASC, id ASC"
    )
    fun observeActiveItems(): Flow<List<ItemEntity>>

    @Query(
        "SELECT * FROM items WHERE deletedAt IS NULL AND state = 'completed' " +
            "ORDER BY completedAt DESC LIMIT 100"
    )
    fun observeCompletedItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: String): ItemEntity?

    @Insert
    suspend fun insertAll(items: List<ItemEntity>)

    @Update
    suspend fun update(item: ItemEntity)

    @Query("UPDATE items SET labelId = :targetLabelId, updatedAt = :now WHERE labelId = :sourceLabelId AND deletedAt IS NULL")
    suspend fun reassignLabel(sourceLabelId: String, targetLabelId: String?, now: Long)

    // "Everything after the hundredth" — LIMIT -1 OFFSET 100 is SQLite's
    // idiom for an unbounded limit with an offset.
    @Query(
        """
        UPDATE items SET deletedAt = :now, updatedAt = :now
        WHERE id IN (
          SELECT id FROM items
          WHERE deletedAt IS NULL AND state = 'completed'
          ORDER BY completedAt DESC
          LIMIT -1 OFFSET 100
        )
        """
    )
    suspend fun trimCompletedBeyond100(now: Long)

    @Query("UPDATE items SET deletedAt = :now, updatedAt = :now WHERE deletedAt IS NULL AND state = 'completed'")
    suspend fun tombstoneAllCompleted(now: Long)
}
