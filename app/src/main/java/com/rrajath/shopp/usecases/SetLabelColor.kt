package com.rrajath.shopp.usecases

import androidx.room.withTransaction
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.LabelRepository

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
