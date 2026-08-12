package com.rrajath.shopp.usecases

import androidx.room.withTransaction
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.LabelRepository
import com.rrajath.shopp.domain.foldForMatching

class RenameLabel(
    private val database: ShoppDatabase,
    private val labelRepository: LabelRepository,
) {
    sealed class Result {
        data object Success : Result()
        data object NameTaken : Result()
    }

    suspend operator fun invoke(labelId: String, newName: String): Result =
        database.withTransaction {
            val label = labelRepository.getById(labelId) ?: return@withTransaction Result.Success
            val folded = newName.foldForMatching()
            val collision = labelRepository.findByFoldedName(folded)
            if (collision != null && collision.id != labelId) return@withTransaction Result.NameTaken
            labelRepository.rename(label, newName)
            Result.Success
        }
}
