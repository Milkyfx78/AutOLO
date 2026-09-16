package com.damsel.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Query("SELECT * FROM books ORDER BY importedAt DESC")
    fun observeBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBook(id: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<BookPageEntity>)

    @Query("SELECT * FROM book_pages WHERE bookId = :bookId ORDER BY pageIndex")
    suspend fun getPages(bookId: String): List<BookPageEntity>

    @Query("SELECT COUNT(*) FROM book_pages WHERE bookId = :bookId")
    suspend fun pageCountFor(bookId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ReadingProgressEntity)

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    suspend fun getProgress(bookId: String): ReadingProgressEntity?

    @Query("UPDATE books SET genre = :genre WHERE id = :bookId")
    suspend fun setGenre(bookId: String, genre: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE id = :id")
    suspend fun deleteHighlight(id: String)

    @Query("SELECT * FROM highlights WHERE bookId = :bookId AND pageIndex = :pageIndex ORDER BY startOffset")
    fun observeHighlights(bookId: String, pageIndex: Int): Flow<List<HighlightEntity>>
}
