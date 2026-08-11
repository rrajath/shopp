package com.rrajath.milk.usecases

import androidx.room.withTransaction
import com.rrajath.milk.data.db.ShoppDatabase
import com.rrajath.milk.data.repository.LabelRepository

class SetLabelColor(
    private val database: ShoppDatabase,
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(labelId: String, colorIndex: Int) {
        database.withTransaction {
            val label = labelRepository.getById(labelId) ?: return@withTransaction
            labelRepository.setColor(label, colorIndex)
        }
    }
}
