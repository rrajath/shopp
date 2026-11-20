package com.rrajath.shopp.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = LabelEntity::class,
            parentColumns = ["id"],
            childColumns = ["labelId"],
            onDelete = ForeignKey.RESTRICT,
        )
    ],
    // Room wants an index on the FK child column; the query-shaped partial
    // indexes (ix_items_active etc.) are created separately in onCreate.
    indices = [Index(value = ["labelId"])],
)
data class ItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val labelId: String?,
    val state: String, // STATE_ACTIVE | STATE_COMPLETED
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val deletedAt: Long?,
    // defaultValue must match MIGRATION_1_2's column default, or Room's
    // schema validation fails on upgraded databases.
    @ColumnInfo(defaultValue = "1") val quantity: Int = 1,
)
