package com.rrajath.shopp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rrajath.shopp.BuildConfig
import com.rrajath.shopp.domain.foldForMatching

@Database(
    entities = [ItemEntity::class, LabelEntity::class, PurchaseHistoryEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class ShoppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun labelDao(): LabelDao
    abstract fun purchaseHistoryDao(): PurchaseHistoryDao

    companion object {
        private const val DB_NAME = "shopping.db"

        // Partial indexes: every read filters deletedAt IS NULL, so
        // tombstones shouldn't be in the hot index. Room's @Index can't
        // express a WHERE clause, so these live outside Room's schema --
        // which means Room's post-migration check sees them as unexpected
        // and throws "Migration didn't properly handle". Every Migration
        // must therefore drop them first (dropPartialIndexes); onOpen,
        // which runs after the check, puts them back.
        private val PARTIAL_INDEXES = listOf(
            "ux_labels_folded" to
                "CREATE UNIQUE INDEX IF NOT EXISTS ux_labels_folded ON labels(nameFolded) WHERE deletedAt IS NULL",
            "ix_items_active" to
                "CREATE INDEX IF NOT EXISTS ix_items_active ON items(labelId, createdAt, id) " +
                "WHERE deletedAt IS NULL AND state = 'active'",
            "ix_items_completed" to
                "CREATE INDEX IF NOT EXISTS ix_items_completed ON items(completedAt DESC) " +
                "WHERE deletedAt IS NULL AND state = 'completed'",
            "ix_labels_recent" to
                "CREATE INDEX IF NOT EXISTS ix_labels_recent ON labels(lastUsedAt DESC) WHERE deletedAt IS NULL",
        )

        private fun createPartialIndexes(db: SupportSQLiteDatabase) {
            PARTIAL_INDEXES.forEach { (_, sql) -> db.execSQL(sql) }
        }

        private fun dropPartialIndexes(db: SupportSQLiteDatabase) {
            PARTIAL_INDEXES.forEach { (name, _) -> db.execSQL("DROP INDEX IF EXISTS $name") }
        }

        // v2: per-item quantity. Existing rows become 1, which displays
        // exactly as before (no "(n)" suffix).
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                dropPartialIndexes(db)
                db.execSQL("ALTER TABLE items ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1")
            }
        }

        // v3: purchase_history for Quick Add's Frequent/Recent lists, seeded
        // from every completed row still on disk (including ones "Clear"
        // tombstoned) so upgraders don't start with empty lists. Titles are
        // folded in Kotlin, not SQL, to match foldForMatching exactly.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                dropPartialIndexes(db)
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `purchase_history` (`titleFolded` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, `labelId` TEXT, `count` INTEGER NOT NULL, " +
                        "`lastPurchasedAt` INTEGER NOT NULL, `previousPurchasedAt` INTEGER, " +
                        "PRIMARY KEY(`titleFolded`))"
                )
                seedPurchaseHistory(db)
            }
        }

        private fun seedPurchaseHistory(db: SupportSQLiteDatabase) {
            // Oldest first, so the last write per title holds its latest
            // casing, label, and time.
            val byTitle = LinkedHashMap<String, PurchaseHistoryEntity>()
            db.query(
                "SELECT title, labelId, completedAt FROM items " +
                    "WHERE state = 'completed' AND completedAt IS NOT NULL ORDER BY completedAt ASC"
            ).use { c ->
                while (c.moveToNext()) {
                    val title = c.getString(0)
                    val labelId = if (c.isNull(1)) null else c.getString(1)
                    val completedAt = c.getLong(2)
                    val key = title.foldForMatching()
                    val prev = byTitle[key]
                    byTitle[key] = PurchaseHistoryEntity(
                        titleFolded = key,
                        title = title,
                        labelId = labelId,
                        count = (prev?.count ?: 0) + 1,
                        lastPurchasedAt = completedAt,
                        previousPurchasedAt = prev?.lastPurchasedAt,
                    )
                }
            }
            byTitle.values.forEach { e ->
                db.execSQL(
                    "INSERT INTO purchase_history " +
                        "(titleFolded, title, labelId, count, lastPurchasedAt, previousPurchasedAt) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(e.titleFolded, e.title, e.labelId, e.count, e.lastPurchasedAt, e.previousPurchasedAt),
                )
            }
        }

        // name is overridable only so migration tests can use a fresh file each.
        fun build(context: Context, name: String = DB_NAME): ShoppDatabase =
            Room.databaseBuilder(context.applicationContext, ShoppDatabase::class.java, name)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(Callback)
                .build()

        // Same schema/indexes/triggers, no file on disk — used by tests.
        fun buildInMemory(context: Context): ShoppDatabase =
            Room.inMemoryDatabaseBuilder(context, ShoppDatabase::class.java)
                .addCallback(Callback)
                .build()
    }

    private object Callback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            createPartialIndexes(db)

            if (BuildConfig.DEBUG) {
                db.execSQL(guardTriggerSql(table = "items", trigger = "dbg_items_updated_at"))
                db.execSQL(guardTriggerSql(table = "labels", trigger = "dbg_labels_updated_at"))
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // PRAGMA busy_timeout returns the new value as a one-row result
            // set, so it needs query() rather than execSQL() (the latter
            // rejects statements that produce rows).
            db.query("PRAGMA busy_timeout = 3000").close()
            // No-op normally; restores the indexes a migration dropped.
            createPartialIndexes(db)
        }

        // Debug-only defense in depth: every repository write must bump
        // updatedAt from the injected Clock -- a silently
        // stale updatedAt is invisible until V2 sync depends on it.
        private fun guardTriggerSql(table: String, trigger: String): String =
            """
            CREATE TRIGGER $trigger BEFORE UPDATE ON $table
            WHEN NEW.updatedAt <= OLD.updatedAt
            BEGIN
              SELECT RAISE(ABORT, 'updatedAt not bumped');
            END
            """.trimIndent()
    }
}
