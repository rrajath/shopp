package com.rrajath.shopp.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Opens a real v1 database file (as shipped: Room's v1 schema plus the
 * hand-made partial indexes from the onCreate callback) with the
 * production builder, so the migration and Room's post-migration schema
 * check run exactly as they do on an upgraded install.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShoppMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    // A fresh file per test: Robolectric can keep a pooled connection to a
    // closed database, so reusing one name lets a previous test's file leak in.
    private val dbName = "migration-${java.util.UUID.randomUUID()}.db"

    @Before
    fun setUp() {
        createV1Database()
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun `v1 database migrates to v2 with existing items at quantity 1`() = runBlocking {
        val db = ShoppDatabase.build(context, dbName)
        try {
            val items = db.itemDao().getActiveItems()
            assertEquals(listOf("milk"), items.map { it.title })
            assertEquals(1, items.single().quantity)
        } finally {
            db.close()
        }
    }

    @Test
    fun `partial indexes exist again after migrating`() = runBlocking {
        val db = ShoppDatabase.build(context, dbName)
        try {
            db.itemDao().getActiveItems() // forces open + migration
            val names = mutableListOf<String>()
            db.openHelper.readableDatabase
                .query("SELECT name FROM sqlite_master WHERE type = 'index'")
                .use { c -> while (c.moveToNext()) names += c.getString(0) }
            assertTrue(
                names.toString(),
                names.containsAll(
                    listOf("ux_labels_folded", "ix_items_active", "ix_items_completed", "ix_labels_recent"),
                ),
            )
        } finally {
            db.close()
        }
    }

    @Test
    fun `v2 to v3 seeds purchase history from completed rows, cleared ones included`() = runBlocking {
        val file = context.getDatabasePath(dbName)
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            fun completed(id: String, title: String, at: Long, deletedAt: Long?) = db.execSQL(
                "INSERT INTO items (id, title, labelId, state, createdAt, updatedAt, completedAt, deletedAt) " +
                    "VALUES ('$id', '$title', NULL, 'completed', 1, 1, $at, ${deletedAt ?: "NULL"})"
            )
            completed("c1", "Eggs", 100, deletedAt = 500)
            completed("c2", "eggs", 200, deletedAt = null)
            completed("c3", "bread", 150, deletedAt = null)
        }
        val db = ShoppDatabase.build(context, dbName)
        try {
            val dao = db.purchaseHistoryDao()
            val eggs = dao.get("eggs")!!
            assertEquals(2, eggs.count)
            assertEquals("eggs", eggs.title)
            assertEquals(200L, eggs.lastPurchasedAt)
            assertEquals(100L, eggs.previousPurchasedAt)
            assertEquals(1, dao.get("bread")!!.count)
            assertEquals(null, dao.get("milk")) // active, never purchased
        } finally {
            db.close()
        }
    }

    // Statements copied from app/schemas/.../1.json plus the v1 onCreate
    // callback's partial indexes.
    private fun createV1Database() {
        val file = context.getDatabasePath(dbName).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `items` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                    "`labelId` TEXT, `state` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, `completedAt` INTEGER, `deletedAt` INTEGER, " +
                    "PRIMARY KEY(`id`), FOREIGN KEY(`labelId`) REFERENCES `labels`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE RESTRICT )"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_labelId` ON `items` (`labelId`)")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `labels` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                    "`nameFolded` TEXT NOT NULL, `colorIndex` INTEGER NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                    "`lastUsedAt` INTEGER NOT NULL, `deletedAt` INTEGER, PRIMARY KEY(`id`))"
            )
            db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            db.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id,identity_hash) " +
                    "VALUES(42, 'd0ba197ea646b9172977e38c47bd33ac')"
            )
            db.execSQL("CREATE UNIQUE INDEX ux_labels_folded ON labels(nameFolded) WHERE deletedAt IS NULL")
            db.execSQL(
                "CREATE INDEX ix_items_active ON items(labelId, createdAt, id) " +
                    "WHERE deletedAt IS NULL AND state = 'active'"
            )
            db.execSQL(
                "CREATE INDEX ix_items_completed ON items(completedAt DESC) " +
                    "WHERE deletedAt IS NULL AND state = 'completed'"
            )
            db.execSQL("CREATE INDEX ix_labels_recent ON labels(lastUsedAt DESC) WHERE deletedAt IS NULL")
            db.execSQL(
                "INSERT INTO items (id, title, labelId, state, createdAt, updatedAt, completedAt, deletedAt) " +
                    "VALUES ('i1', 'milk', NULL, 'active', 1000, 1000, NULL, NULL)"
            )
            db.version = 1
        }
    }
}
