package com.damsel.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.damsel.app.data.BOOK_GENRE_PRESETS
import com.damsel.app.data.BookEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenBook: (String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val books by viewModel.books.collectAsState()
    val importState by viewModel.importState.collectAsState()
    var activeGenre by rememberSaveable { mutableStateOf<String?>(null) }
    var taggingBook by remember { mutableStateOf<BookEntity?>(null) }

    val pickPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importPdf(it, onOpenBook) }
    }

    val genresInLibrary = books.mapNotNull { it.genre }.distinct().sorted()
    val visibleBooks = if (activeGenre == null) books else books.filter { it.genre == activeGenre }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your library", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(if (importState is ImportState.Importing) "Importing…" else "Add a book") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = { pickPdf.launch(arrayOf("application/pdf")) }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (genresInLibrary.isNotEmpty()) {
                GenreChipRow(
                    genres = genresInLibrary,
                    active = activeGenre,
                    onSelect = { activeGenre = it }
                )
            }
            Box(Modifier.fillMaxSize()) {
                when {
                    importState is ImportState.Failed -> {
                        Text(
                            (importState as ImportState.Failed).message,
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    visibleBooks.isEmpty() && importState !is ImportState.Importing -> {
                        EmptyLibrary(Modifier.align(Alignment.Center))
                    }
                    else -> {
                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            items(visibleBooks, key = { it.id }) { book ->
                                BookRow(
                                    book,
                                    onClick = { onOpenBook(book.id) },
                                    onLongClick = { taggingBook = book }
                                )
                            }
                            if (importState is ImportState.Importing) {
                                item { ImportingRow() }
                            }
                        }
                    }
                }
            }
        }
    }

    taggingBook?.let { book ->
        GenreTagDialog(
            book = book,
            onPick = { genre -> viewModel.setGenre(book.id, genre); taggingBook = null },
            onDismiss = { taggingBook = null }
        )
    }
}

@Composable
private fun GenreChipRow(genres: List<String>, active: String?, onSelect: (String?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(selected = active == null, onClick = { onSelect(null) }, label = { Text("All") })
        }
        items(genres) { genre ->
            FilterChip(selected = active == genre, onClick = { onSelect(genre) }, label = { Text(genre) })
        }
    }
}

@Composable
private fun GenreTagDialog(book: BookEntity, onPick: (String?) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tag “${book.title}”") },
        text = {
            Column {
                Text(
                    "Pick a mood/genre so it shows up when you filter your library — this is just for your own shelf, not a catalog.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                GenrePresetGrid(genres = BOOK_GENRE_PRESETS, current = book.genre, onPick = onPick)
            }
        },
        confirmButton = {
            TextButton(onClick = { onPick(null) }) { Text("Clear tag") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

/** A tiny wrapping chip grid — avoids pulling in the experimental FlowRow API for one dialog. */
@Composable
private fun GenrePresetGrid(genres: List<String>, current: String?, onPick: (String?) -> Unit) {
    val rows = genres.chunked(3)
    Column(Modifier.padding(top = 12.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                row.forEach { genre ->
                    FilterChip(selected = current == genre, onClick = { onPick(genre) }, label = { Text(genre) })
                }
            }
        }
    }
}

@Composable
internal fun EmptyLibrary(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Filled.MenuBook,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 12.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text("Nothing here yet", style = MaterialTheme.typography.titleLarge)
        Text(
            "Add a PDF to start reading with Damsel.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BookRow(book: BookEntity, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(Icons.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(book.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull("${book.pageCount} pages", book.genre).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ImportingRow() {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(2.dp))
        Text("Reading your document…", style = MaterialTheme.typography.bodyMedium)
    }
}
