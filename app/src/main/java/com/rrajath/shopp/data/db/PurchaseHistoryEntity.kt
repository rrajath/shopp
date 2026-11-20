package com.rrajath.shopp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// One row per distinct item title ever completed, feeding Quick Add's
// Frequent and Recent lists. Kept apart from `items` because completed rows
// are trimmed past 100 and tombstoned by "Clear", while purchase counts
// should outlive both.
//
// labelId has no foreign key: label delete/merge rewrite it explicitly
// (see DeleteLabel/MergeLabels), and readers treat an id that isn't a live
// label as Inbox, so a stale id is harmless.
@Entity(tableName = "purchase_history")
data class PurchaseHistoryEntity(
    @PrimaryKey val titleFolded: String,
    // Display title as last completed (keeps the user's latest casing).
    val title: String,
    // Label it was last completed under; null = Inbox.
    val labelId: String?,
    val count: Int,
    val lastPurchasedAt: Long,
    // lastPurchasedAt before the latest completion, so undoing that
    // completion can put the row back where it was in Recent.
    val previousPurchasedAt: Long?,
)
