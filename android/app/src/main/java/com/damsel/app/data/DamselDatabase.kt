package com.damsel.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BookEntity::class, ReadingProgressEntity::class, BookPageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DamselDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}
