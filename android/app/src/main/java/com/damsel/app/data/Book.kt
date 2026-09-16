package com.damsel.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One imported document. [uri] is the persisted SAF content URI we re-open the source
 * PDF from; extracted page text is cached in [BookPageEntity] so we don't re-parse on
 * every open.
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val uri: String,
    val pageCount: Int,
    val importedAt: Long,
    /** User-assigned, e.g. "Romance", "Sci-Fi" — powers the mood/genre filter chips on Library. */
    val genre: String? = null
)

/** Presets offered when tagging a book — the user's own shelf, not a fetched catalog. */
val BOOK_GENRE_PRESETS = listOf(
    "Romance", "Fantasy", "Sci-Fi", "Mystery", "Thriller",
    "Non-fiction", "Business", "Academic", "Self-help", "Biography"
)

/** Real reading position — the only source of truth for "where the user left off" (spec §42). */
@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val bookId: String,
    val lastPageIndex: Int,
    val updatedAt: Long
)

@Entity(tableName = "book_pages", primaryKeys = ["bookId", "pageIndex"])
data class BookPageEntity(
    val bookId: String,
    val pageIndex: Int,
    val text: String
)
