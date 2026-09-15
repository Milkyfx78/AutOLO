package com.damsel.app.data

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Extracts real per-page text from a PDF so AI features have something real to work on (spec §65). */
class PdfTextExtractor @Inject constructor(private val context: Context) {

    data class Extracted(val title: String, val pageTexts: List<String>)

    suspend fun extract(uri: Uri): Extracted = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Couldn't open that file." }
            PDDocument.load(input).use { document ->
                val stripper = PDFTextStripper()
                val pages = mutableListOf<String>()
                for (pageIndex in 1..document.numberOfPages) {
                    stripper.startPage = pageIndex
                    stripper.endPage = pageIndex
                    pages += stripper.getText(document).trim()
                }
                val title = document.documentInformation?.title?.takeIf { it.isNotBlank() }
                    ?: displayNameOf(uri)
                Extracted(title, pages)
            }
        }
    }

    private fun displayNameOf(uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) return cursor.getString(nameIndex)
        }
        return uri.lastPathSegment ?: "Untitled document"
    }
}
