package com.damsel.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.damsel.app.data.BookEntity
import com.damsel.app.ui.screens.BookRow
import com.damsel.app.ui.screens.EmptyLibrary
import com.damsel.app.ui.screens.ReaderToolbar
import com.damsel.app.ui.screens.SpeechState
import com.damsel.app.ui.theme.DamselTheme
import com.damsel.app.voice.VoiceEmotion
import org.junit.Rule
import org.junit.Test

/**
 * These are real renders of the app's actual screen composables (theme, typography, the same
 * BookRow/EmptyLibrary/ReaderToolbar functions the app ships) — run on the JVM via Paparazzi,
 * with no emulator or device involved. They stand in for a device screenshot in an environment
 * with no hardware virtualization available for a real Android emulator.
 */
class ScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun libraryWithBooks() {
        paparazzi.snapshot {
            DamselTheme {
                val books = listOf(
                    BookEntity("1", "Atomic Habits", "content://sample", 224, 0L),
                    BookEntity("2", "The Pragmatic Programmer", "content://sample", 352, 0L),
                    BookEntity("3", "A Research Paper on Retrieval-Augmented Generation", "content://sample", 18, 0L)
                )
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Your library", style = MaterialTheme.typography.headlineMedium) },
                            actions = {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text("Add a book") },
                            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                            onClick = {}
                        )
                    }
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            items(books) { book -> BookRow(book, onClick = {}) }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun libraryEmpty() {
        paparazzi.snapshot {
            DamselTheme {
                androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        EmptyLibrary(Modifier)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun readerPage() {
        paparazzi.snapshot {
            DamselTheme {
                val pageText = "Chapter One: The Beginning\n\n" +
                    "Damsel opened its eyes on a device with no hardware acceleration, and began to read anyway.\n\n" +
                    "This page is rendered by the same BasicTextField and typography the real Reader screen uses " +
                    "— selectable, scrollable, and readable."
                var fieldValue by remember { mutableStateOf(TextFieldValue(pageText)) }
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text("demo.pdf", maxLines = 1)
                                    Text("Page 1 of 12", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                    },
                    bottomBar = {
                        ReaderToolbar(
                            canGoBack = false,
                            canGoForward = true,
                            onPrev = {},
                            onNext = {},
                            speech = SpeechState.Idle,
                            onSpeak = {},
                            onStop = {},
                            onAsk = {}
                        )
                    }
                ) { padding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        BasicTextField(
                            value = fieldValue,
                            onValueChange = { fieldValue = it },
                            readOnly = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun readerPageWithHighlight() {
        paparazzi.snapshot {
            DamselTheme {
                val pageText = "“In vain have I struggled. It will not do. My feelings will not be repressed. " +
                    "You must allow me to tell you how ardently I admire and love you.”\n\n" +
                    "Elizabeth's astonishment was beyond expression."
                val annotated = androidx.compose.ui.text.buildAnnotatedString {
                    append(pageText)
                    addStyle(
                        androidx.compose.ui.text.SpanStyle(
                            background = androidx.compose.ui.graphics.Color(com.damsel.app.data.HighlightColor.YELLOW.argb).copy(alpha = 0.55f)
                        ),
                        pageText.indexOf("My feelings"),
                        pageText.indexOf("love you.") + "love you.".length
                    )
                }
                var fieldValue by remember { mutableStateOf(TextFieldValue(annotated)) }
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("pride_and_prejudice.pdf", maxLines = 1) })
                    },
                    bottomBar = {
                        Column {
                            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                                androidx.compose.material3.Slider(value = 42f, onValueChange = {}, valueRange = 0f..120f)
                                androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                                    Text("0:42", style = MaterialTheme.typography.labelSmall)
                                    Text("2:00", style = MaterialTheme.typography.labelSmall)
                                }
                                androidx.compose.foundation.layout.Row(
                                    Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                ) {
                                    Icon(androidx.compose.material.icons.Icons.Filled.Replay10, contentDescription = null, modifier = Modifier.padding(horizontal = 16.dp))
                                    Icon(androidx.compose.material.icons.Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(32.dp))
                                    Icon(androidx.compose.material.icons.Icons.Filled.Forward10, contentDescription = null, modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                            ReaderToolbar(
                                canGoBack = true, canGoForward = true, onPrev = {}, onNext = {},
                                speech = SpeechState.Playing, onSpeak = {}, onStop = {}, onAsk = {}
                            )
                        }
                    }
                ) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        BasicTextField(
                            value = fieldValue,
                            onValueChange = { fieldValue = it },
                            readOnly = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun voiceStudioChips() {
        paparazzi.snapshot {
            DamselTheme {
                androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    Text("Voice Studio", style = MaterialTheme.typography.titleLarge)
                    Text("Voice", style = MaterialTheme.typography.labelSmall)
                    androidx.compose.foundation.layout.Row(
                        Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Damsel", "Merlin", "Aurora").forEachIndexed { index, name ->
                            FilterChip(selected = index == 0, onClick = {}, label = { Text(name) })
                        }
                    }
                    Text("Emotion", style = MaterialTheme.typography.labelSmall)
                    androidx.compose.foundation.layout.Row(
                        Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        VoiceEmotion.entries.take(3).forEachIndexed { index, emotion ->
                            FilterChip(selected = index == 1, onClick = {}, label = { Text(emotion.label) })
                        }
                    }
                }
                }
            }
        }
    }
}

