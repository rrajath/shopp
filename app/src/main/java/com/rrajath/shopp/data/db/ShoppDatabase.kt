package com.rrajath.shopp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rrajath.shopp.BuildConfig

@Database(entities = [ItemEntity::class, LabelEntity::class], version = 1, exportSchema = true)
abstract class ShoppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun labelDao(): LabelDao

    companion object {
        private const val DB_NAME = "shopping.db"

        fun build(context: Context): ShoppDatabase =
            Room.databaseBuilder(context.applicationContext, ShoppDatabase::class.java, DB_NAME)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
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
            // Partial indexes: every read filters deletedAt IS NULL, so
            // tombstones shouldn't be in the hot index (see TDD §3.1).
            db.execSQL(
                "CREATE UNIQUE INDEX ux_labels_folded ON labels(nameFolded) WHERE deletedAt IS NULL"
            )
            db.execSQL(
                "CREATE INDEX ix_items_active ON items(labelId, createdAt, id) " +
                    "WHERE deletedAt IS NULL AND state = 'active'"
            )
            db.execSQL(
                "CREATE INDEX ix_items_completed ON items(completedAt DESC) " +
                    "WHERE deletedAt IS NULL AND state = 'completed'"
            )
            db.execSQL(
                "CREATE INDEX ix_labels_recent ON labels(lastUsedAt DESC) WHERE deletedAt IS NULL"
            )

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
        }

        // Debug-only defense in depth: every repository write must bump
        // updatedAt from the injected Clock. See TDD §3.5 — a silently
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
