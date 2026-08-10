package com.rrajath.milk.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {

    @Query("SELECT * FROM labels WHERE deletedAt IS NULL ORDER BY lastUsedAt DESC")
    fun observeLabels(): Flow<List<LabelEntity>>

    @Query("SELECT * FROM labels WHERE nameFolded = :nameFolded AND deletedAt IS NULL LIMIT 1")
    suspend fun findByFoldedName(nameFolded: String): LabelEntity?

    @Query("SELECT colorIndex FROM labels WHERE deletedAt IS NULL")
    suspend fun getLiveColorIndices(): List<Int>

    @Query("SELECT * FROM labels WHERE id = :id")
    suspend fun getById(id: String): LabelEntity?

    @Insert
    suspend fun insert(label: LabelEntity)

    @Update
    suspend fun update(label: LabelEntity)

    @Query("UPDATE labels SET lastUsedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun touch(id: String, now: Long)

    @Query("UPDATE labels SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun tombstone(id: String, now: Long)
}
