package com.damsel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.damsel.app.voice.VoiceEmotion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAsk by rememberSaveable { mutableStateOf(false) }
    var showVoiceStudio by rememberSaveable { mutableStateOf(false) }

    remember(bookId) { viewModel.load(bookId); true }

    val pageText = state.pages.getOrNull(state.currentPage).orEmpty()
    var fieldValue by remember(state.currentPage, pageText) { mutableStateOf(TextFieldValue(pageText)) }
    val selectedText = fieldValue.text.substring(
        minOf(fieldValue.selection.min, fieldValue.text.length),
        minOf(fieldValue.selection.max, fieldValue.text.length)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.title, maxLines = 1)
                        if (state.pages.isNotEmpty()) {
                            Text(
                                "Page ${state.currentPage + 1} of ${state.pages.size}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showVoiceStudio = true }) {
                        Icon(Icons.Filled.GraphicEq, contentDescription = "Voice Studio")
                    }
                }
            )
        },
        bottomBar = {
            ReaderToolbar(
                canGoBack = state.currentPage > 0,
                canGoForward = state.currentPage < state.pages.size - 1,
                onPrev = { viewModel.goToPage(state.currentPage - 1) },
                onNext = { viewModel.goToPage(state.currentPage + 1) },
                speech = state.speech,
                onSpeak = viewModel::speakCurrentPage,
                onStop = viewModel::stopSpeaking,
                onAsk = { showAsk = true }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.padding(32.dp))
                state.loadError != null -> Text(
                    state.loadError!!,
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.error
                )
                else -> Column(
                    Modifier
                        .fillMaxSize()
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

    if (showVoiceStudio) {
        VoiceStudioSheet(
            state = state,
            onSelectVoice = viewModel::selectVoice,
            onSelectEmotion = viewModel::selectEmotion,
            onDismiss = { showVoiceStudio = false }
        )
    }

    if (showAsk) {
        AskDamselSheet(
            selection = selectedText,
            exchange = state.ask,
            onAsk = { question -> viewModel.ask(question, selectedText.ifBlank { null }) },
            onDismiss = { showAsk = false; viewModel.dismissAsk() }
        )
    }
}

@Composable
private fun ReaderToolbar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    speech: SpeechState,
    onSpeak: () -> Unit,
    onStop: () -> Unit,
    onAsk: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev, enabled = canGoBack) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous page")
        }
        Button(onClick = onAsk) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("Ask Damsel")
        }
        when (speech) {
            is SpeechState.Generating -> CircularProgressIndicator(Modifier.size(24.dp))
            is SpeechState.Playing -> IconButton(onClick = onStop) {
                Icon(Icons.Filled.Stop, contentDescription = "Stop")
            }
            else -> IconButton(onClick = onSpeak) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Read aloud")
            }
        }
        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next page")
        }
    }
    if (speech is SpeechState.Failed) {
        Text(
            speech.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceStudioSheet(
    state: ReaderUiState,
    onSelectVoice: (com.damsel.app.voice.DamselVoice) -> Unit,
    onSelectEmotion: (VoiceEmotion) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp)) {
            Text("Voice Studio", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Text("Voice", style = MaterialTheme.typography.labelSmall)
            Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.voices.forEach { voice ->
                    FilterChip(
                        selected = state.selectedVoice?.id == voice.id,
                        onClick = { onSelectVoice(voice) },
                        label = { Text(voice.displayName) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Emotion", style = MaterialTheme.typography.labelSmall)
            Row(
                Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceEmotion.entries.take(3).forEach { emotion ->
                    FilterChip(
                        selected = state.emotion == emotion,
                        onClick = { onSelectEmotion(emotion) },
                        label = { Text(emotion.label) }
                    )
                }
            }
            Row(Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VoiceEmotion.entries.drop(3).forEach { emotion ->
                    FilterChip(
                        selected = state.emotion == emotion,
                        onClick = { onSelectEmotion(emotion) },
                        label = { Text(emotion.label) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AskDamselSheet(
    selection: String,
    exchange: AskExchange?,
    onAsk: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var question by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp)) {
            Text("Ask Damsel", style = MaterialTheme.typography.titleLarge)
            if (selection.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "About your selection: “${selection.take(120)}”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Your question") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { onAsk(question) }, enabled = question.isNotBlank()) {
                Text("Ask")
            }
            Spacer(Modifier.height(16.dp))
            when {
                exchange?.isLoading == true -> CircularProgressIndicator(Modifier.size(24.dp))
                exchange?.error != null -> Text(exchange.error, color = MaterialTheme.colorScheme.error)
                exchange?.answer != null -> Text(exchange.answer, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
