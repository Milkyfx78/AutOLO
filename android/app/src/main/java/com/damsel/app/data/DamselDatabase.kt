package com.damsel.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BookEntity::class, ReadingProgressEntity::class, BookPageEntity::class, HighlightEntity::class],
    version = 2,
    exportSchema = false
)
abstract class DamselDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}

/**
 * v1 -> v2 added `genre` to books and the whole `highlights` table. This must be a real
 * migration, not a destructive fallback: a destructive fallback drops every table on any schema
 * change, which silently deletes the user's entire library and reading progress the moment they
 * install an update — exactly the kind of "the app doesn't work" bug that's unacceptable to ship.
 */
val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE books ADD COLUMN genre TEXT")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS highlights (
                id TEXT NOT NULL PRIMARY KEY,
                bookId TEXT NOT NULL,
                pageIndex INTEGER NOT NULL,
                startOffset INTEGER NOT NULL,
                endOffset INTEGER NOT NULL,
                quotedText TEXT NOT NULL,
                color TEXT NOT NULL,
                note TEXT,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
