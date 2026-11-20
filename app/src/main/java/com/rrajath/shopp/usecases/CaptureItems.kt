package com.rrajath.shopp.usecases

import androidx.room.withTransaction
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.data.db.STATE_ACTIVE
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.ItemRepository
import com.rrajath.shopp.data.repository.LabelRepository
import com.rrajath.shopp.domain.Clock
import com.rrajath.shopp.domain.IdGenerator
import com.rrajath.shopp.domain.LabelRef
import com.rrajath.shopp.domain.clampQuantity
import com.rrajath.shopp.domain.foldForMatching
import com.rrajath.shopp.domain.parseCapture

// Resolve/create labels -> insert N items -> bump last_used_at on
// each touched label, one transaction. Partial failure is not observable.
// A line whose folded title matches an active item under the same label
// (or an earlier line in the same paste) is dropped and reported back in
// Result.duplicates as the item it matched, so the UI can show the count
// already on the list.
//
// quantity applies to every inserted line. labelOverrides maps a folded
// title to the label it should land under (from picking a history
// suggestion); it only applies to lines without their own `@token`, never
// changes sticky, and doesn't bump that label's lastUsedAt (touching it
// here could double-write a label a token on another line also touches).
class CaptureItems(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
    private val labelRepository: LabelRepository,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) {
    data class Result(
        val items: List<ItemEntity>,
        val newSticky: LabelRef,
        val duplicates: List<ItemEntity> = emptyList(),
        // True when an `@token` in this input set newSticky, so callers can
        // tell an explicit label choice apart from sticky passing through.
        val stickyFromToken: Boolean = false,
    )

    suspend operator fun invoke(
        input: String,
        sticky: LabelRef,
        quantity: Int = 1,
        labelOverrides: Map<String, String?> = emptyMap(),
    ): Result {
        val parsed = parseCapture(input, sticky)
        if (parsed.lines.isEmpty()) return Result(items = emptyList(), newSticky = sticky)
        val itemQuantity = clampQuantity(quantity)

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

            // (labelId, folded title) -> the item holding that slot, so a
            // dropped duplicate can be reported with its existing quantity.
            val activeByKey = itemRepository.getActiveItems()
                .associateByTo(HashMap()) { it.labelId to it.title.foldForMatching() }

            val now = clock.nowMillis()
            val duplicates = mutableListOf<ItemEntity>()
            val items = parsed.lines.mapNotNull { line ->
                val folded = line.title.foldForMatching()
                val labelId = if (!line.hasOwnToken && folded in labelOverrides) {
                    labelOverrides.getValue(folded)
                } else {
                    idFor(line.label)
                }
                val key = labelId to folded
                activeByKey[key]?.let {
                    duplicates += it
                    return@mapNotNull null
                }
                ItemEntity(
                    id = idGenerator.newId(),
                    title = line.title,
                    labelId = labelId,
                    state = STATE_ACTIVE,
                    createdAt = now,
                    updatedAt = now,
                    completedAt = null,
                    deletedAt = null,
                    quantity = itemQuantity,
                ).also { activeByKey[key] = it }
            }
            itemRepository.insertAll(items)

            val newStickyId = idFor(parsed.sticky)
            idPassthroughLabels.forEach { labelRepository.touch(it) }

            Result(
                items = items,
                newSticky = newStickyId?.let { LabelRef.Id(it) } ?: LabelRef.None,
                duplicates = duplicates,
                stickyFromToken = parsed.sticky is LabelRef.Token,
            )
        }
    }
}
