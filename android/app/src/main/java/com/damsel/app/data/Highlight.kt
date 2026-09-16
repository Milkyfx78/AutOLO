package com.damsel.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A real, persisted highlight — spec §38: color, optional note, tied to an exact text range. */
@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val pageIndex: Int,
    val startOffset: Int,
    val endOffset: Int,
    val quotedText: String,
    val color: String,
    val note: String?,
    val createdAt: Long
)

/** The five colors spec §38 calls for. */
enum class HighlightColor(val label: String, val argb: Long) {
    YELLOW("Yellow", 0xFFF6D976),
    BLUE("Blue", 0xFF7FB2F0),
    PURPLE("Purple", 0xFFB79BFF),
    GREEN("Green", 0xFF8FE0AE),
    RED("Red", 0xFFEF9A9A)
}
