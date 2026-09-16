package com.damsel.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BookEntity::class, ReadingProgressEntity::class, BookPageEntity::class, HighlightEntity::class],
    version = 2,
    exportSchema = false
)
abstract class DamselDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}
