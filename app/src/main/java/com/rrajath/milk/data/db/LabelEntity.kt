package com.rrajath.milk.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

const val STATE_ACTIVE = "active"
const val STATE_COMPLETED = "completed"

@Entity(tableName = "labels")
data class LabelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nameFolded: String,
    val colorIndex: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val lastUsedAt: Long,
    val deletedAt: Long?,
)
