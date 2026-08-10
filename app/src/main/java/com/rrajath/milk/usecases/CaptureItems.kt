package com.rrajath.milk.usecases

import androidx.room.withTransaction
import com.rrajath.milk.data.db.ItemEntity
import com.rrajath.milk.data.db.STATE_ACTIVE
import com.rrajath.milk.data.db.ShoppDatabase
import com.rrajath.milk.data.repository.ItemRepository
import com.rrajath.milk.data.repository.LabelRepository
import com.rrajath.milk.domain.Clock
import com.rrajath.milk.domain.IdGenerator
import com.rrajath.milk.domain.LabelRef
import com.rrajath.milk.domain.parseCapture

// TDD §4.3: resolve/create labels -> insert N items -> bump last_used_at on
// each touched label, one transaction. Partial failure is not observable.
class CaptureItems(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
    private val labelRepository: LabelRepository,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) {
    data class Result(val itemsAdded: Int, val newSticky: LabelRef)

    suspend operator fun invoke(input: String, sticky: LabelRef): Result {
        val parsed = parseCapture(input, sticky)
        if (parsed.lines.isEmpty()) return Result(itemsAdded = 0, newSticky = sticky)

        return database.withTransaction {
            val resolvedTokenIds = HashMap<String, String>() // token text -> labelId, memoized per call
            // Labels reached only via sticky Id-passthrough (no token on that
            // line) still need touching — resolveOrCreate already touches
            // token-derived labels itself, so this set must stay disjoint
            // from that to avoid double-writing the same row in one transaction.
            val idPassthroughLabels = HashSet<String>()

            suspend fun idFor(ref: LabelRef): String? = when (ref) {
                is LabelRef.None -> null
                is LabelRef.Id -> {
                    idPassthroughLabels += ref.labelId
                    ref.labelId
                }
                is LabelRef.Token -> resolvedTokenIds.getOrPut(ref.text) {
                    labelRepository.resolveOrCreate(ref.text).id
                }
            }

            val now = clock.nowMillis()
            val items = parsed.lines.map { line ->
                ItemEntity(
                    id = idGenerator.newId(),
                    title = line.title,
                    labelId = idFor(line.label),
                    state = STATE_ACTIVE,
                    createdAt = now,
                    updatedAt = now,
                    completedAt = null,
                    deletedAt = null,
                )
            }
            itemRepository.insertAll(items)

            val newStickyId = idFor(parsed.sticky)
            idPassthroughLabels.forEach { labelRepository.touch(it) }

            Result(
                itemsAdded = items.size,
                newSticky = newStickyId?.let { LabelRef.Id(it) } ?: LabelRef.None,
            )
        }
    }
}
