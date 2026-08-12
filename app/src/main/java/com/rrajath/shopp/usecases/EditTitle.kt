package com.rrajath.shopp.usecases

import androidx.room.withTransaction
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.ItemRepository
import com.rrajath.shopp.data.repository.LabelRepository
import com.rrajath.shopp.domain.LabelRef
import com.rrajath.shopp.domain.parseCapture

// Re-runs token extraction on the new title (PRD §7.4 / TDD §4.3): a token
// re-tags the item and moves it to that section; no token leaves the
// existing label untouched. created_at is never touched, so position is
// preserved (PRD §7.2).
class EditTitle(
    private val database: ShoppDatabase,
    private val itemRepository: ItemRepository,
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(itemId: String, newTitle: String) {
        database.withTransaction {
            val item = itemRepository.getById(itemId) ?: return@withTransaction
            val currentSticky = item.labelId?.let { LabelRef.Id(it) } ?: LabelRef.None
            val line = parseCapture(newTitle, currentSticky).lines.firstOrNull()
                ?: return@withTransaction

            val labelId = when (val ref = line.label) {
                is LabelRef.None -> null
                is LabelRef.Id -> ref.labelId
                is LabelRef.Token -> labelRepository.resolveOrCreate(ref.text).id
            }
            itemRepository.updateTitleAndLabel(item, line.title, labelId)
        }
    }
}
