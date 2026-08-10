package com.rrajath.milk.data.repository

import com.rrajath.milk.data.db.LabelDao
import com.rrajath.milk.data.db.LabelEntity
import com.rrajath.milk.domain.Clock
import com.rrajath.milk.domain.IdGenerator
import com.rrajath.milk.domain.LabelColorAllocator
import com.rrajath.milk.domain.foldForMatching
import kotlinx.coroutines.flow.Flow

class LabelRepository(
    private val labelDao: LabelDao,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) {
    fun observeLabels(): Flow<List<LabelEntity>> = labelDao.observeLabels()

    suspend fun getById(id: String): LabelEntity? = labelDao.getById(id)

    // Resolve-or-create per TDD §4.2. Caller is expected to run this inside
    // its own transaction when part of a larger write (e.g. captureItems).
    suspend fun resolveOrCreate(token: String): LabelEntity {
        val folded = token.foldForMatching()
        labelDao.findByFoldedName(folded)?.let { return it }
        val now = clock.nowMillis()
        val liveIndices = labelDao.getLiveColorIndices()
        val label = LabelEntity(
            id = idGenerator.newId(),
            name = token,
            nameFolded = folded,
            colorIndex = LabelColorAllocator.nextColorIndex(liveIndices),
            createdAt = now,
            updatedAt = now,
            lastUsedAt = now,
            deletedAt = null,
        )
        labelDao.insert(label)
        return label
    }

    suspend fun rename(label: LabelEntity, newName: String) {
        val now = clock.nowMillis()
        labelDao.update(
            label.copy(name = newName, nameFolded = newName.foldForMatching(), updatedAt = now)
        )
    }

    suspend fun touch(id: String) {
        labelDao.touch(id, clock.nowMillis())
    }

    suspend fun tombstone(id: String) {
        labelDao.tombstone(id, clock.nowMillis())
    }
}
