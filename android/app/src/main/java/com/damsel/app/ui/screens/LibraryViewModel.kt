package com.damsel.app.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damsel.app.data.BookDao
import com.damsel.app.data.BookEntity
import com.damsel.app.data.BookPageEntity
import com.damsel.app.data.PdfTextExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface ImportState {
    data object Idle : ImportState
    data object Importing : ImportState
    data class Failed(val message: String) : ImportState
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val extractor: PdfTextExtractor
) : ViewModel() {

    val books = bookDao.observeBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    fun importPdf(uri: Uri, onImported: (String) -> Unit) {
        viewModelScope.launch {
            _importState.value = ImportState.Importing
            runCatching { extractor.extract(uri) }
                .onSuccess { extracted ->
                    val bookId = UUID.randomUUID().toString()
                    bookDao.insertBook(
                        BookEntity(
                            id = bookId,
                            title = extracted.title,
                            uri = uri.toString(),
                            pageCount = extracted.pageTexts.size,
                            importedAt = System.currentTimeMillis()
                        )
                    )
                    bookDao.insertPages(
                        extracted.pageTexts.mapIndexed { index, text ->
                            BookPageEntity(bookId, index, text)
                        }
                    )
                    _importState.value = ImportState.Idle
                    onImported(bookId)
                }
                .onFailure { error ->
                    _importState.value = ImportState.Failed(
                        error.message ?: "Damsel couldn't read that file."
                    )
                }
        }
    }

    fun dismissError() {
        _importState.value = ImportState.Idle
    }
}
