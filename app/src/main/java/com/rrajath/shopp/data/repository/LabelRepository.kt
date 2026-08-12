package com.rrajath.shopp.data.repository

import com.rrajath.shopp.data.db.LabelDao
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.domain.Clock
import com.rrajath.shopp.domain.IdGenerator
import com.rrajath.shopp.domain.LabelColorAllocator
import com.rrajath.shopp.domain.foldForMatching
import kotlinx.coroutines.flow.Flow

class LabelRepository(
    private val labelDao: LabelDao,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) {
    fun observeLabels(): Flow<List<LabelEntity>> = labelDao.observeLabels()

    suspend fun getById(id: String): LabelEntity? = labelDao.getById(id)

    suspend fun findByFoldedName(nameFolded: String): LabelEntity? =
        labelDao.findByFoldedName(nameFolded)

    // Resolve-or-create per TDD §4.2, bumping lastUsedAt either way (PRD §5:
    // "bumped when an item is created with or moved to this label") — this
    // is the single place that touches a token-derived label, so callers
    // must not also call touch() on its result within the same transaction
    // (that would double-write the same row at the same instant and trip
    // the debug updatedAt-guard trigger).
    suspend fun resolveOrCreate(token: String): LabelEntity {
        val folded = token.foldForMatching()
        val now = clock.nowMillis()

        val existing = labelDao.findByFoldedName(folded)
        if (existing != null) {
            if (now <= existing.lastUsedAt) return existing // already current, avoid a no-op write
            val touched = existing.copy(lastUsedAt = now, updatedAt = now)
            labelDao.update(touched)
            return touched
        }

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

    suspend fun setColor(label: LabelEntity, colorIndex: Int) {
        labelDao.update(label.copy(colorIndex = colorIndex, updatedAt = clock.nowMillis()))
    }

    suspend fun touch(id: String) {
        labelDao.touch(id, clock.nowMillis())
    }

    suspend fun tombstone(id: String) {
        labelDao.tombstone(id, clock.nowMillis())
    }
}
