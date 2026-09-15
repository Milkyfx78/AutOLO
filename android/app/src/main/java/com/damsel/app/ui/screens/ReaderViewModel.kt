package com.damsel.app.ui.screens

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damsel.app.ai.AiRouter
import com.damsel.app.data.BookDao
import com.damsel.app.data.ReadingProgressEntity
import com.damsel.app.voice.DamselVoice
import com.damsel.app.voice.VoiceEmotion
import com.damsel.app.voice.VoiceRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AskExchange(val question: String, val answer: String?, val isLoading: Boolean, val error: String? = null)

sealed interface SpeechState {
    data object Idle : SpeechState
    data object Generating : SpeechState
    data object Playing : SpeechState
    data class Failed(val message: String) : SpeechState
}

data class ReaderUiState(
    val title: String = "",
    val pages: List<String> = emptyList(),
    val currentPage: Int = 0,
    val isLoading: Boolean = true,
    val selectedVoice: DamselVoice? = null,
    val voices: List<DamselVoice> = emptyList(),
    val emotion: VoiceEmotion = VoiceEmotion.NEUTRAL,
    val speech: SpeechState = SpeechState.Idle,
    val ask: AskExchange? = null,
    val loadError: String? = null
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val bookDao: BookDao,
    private val aiRouter: AiRouter,
    private val voiceRouter: VoiceRouter
) : ViewModel() {

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private var bookId: String? = null
    private var mediaPlayer: MediaPlayer? = null

    fun load(id: String) {
        if (bookId == id) return
        bookId = id
        viewModelScope.launch {
            val book = bookDao.getBook(id)
            val pages = bookDao.getPages(id).map { it.text }
            val progress = bookDao.getProgress(id)
            val voices = voiceRouter.voices()
            if (book == null) {
                _state.value = _state.value.copy(isLoading = false, loadError = "This book is no longer in your library.")
                return@launch
            }
            _state.value = _state.value.copy(
                title = book.title,
                pages = pages,
                currentPage = (progress?.lastPageIndex ?: 0).coerceIn(0, (pages.size - 1).coerceAtLeast(0)),
                isLoading = false,
                voices = voices,
                selectedVoice = voices.firstOrNull()
            )
        }
    }

    fun goToPage(index: Int) {
        val pages = _state.value.pages
        if (index !in pages.indices) return
        _state.value = _state.value.copy(currentPage = index, ask = null)
        persistProgress(index)
    }

    private fun persistProgress(index: Int) {
        val id = bookId ?: return
        viewModelScope.launch {
            bookDao.upsertProgress(ReadingProgressEntity(id, index, System.currentTimeMillis()))
        }
    }

    fun selectVoice(voice: DamselVoice) {
        _state.value = _state.value.copy(selectedVoice = voice)
    }

    fun selectEmotion(emotion: VoiceEmotion) {
        _state.value = _state.value.copy(emotion = emotion)
    }

    fun ask(question: String, selection: String?) {
        val current = _state.value
        val context = selection?.takeIf { it.isNotBlank() } ?: current.pages.getOrElse(current.currentPage) { "" }
        _state.value = current.copy(ask = AskExchange(question, answer = null, isLoading = true))
        viewModelScope.launch {
            val systemPrompt = "You are Damsel, an intelligent, warm reading companion. Answer using the passage below as context. If the answer isn't in it, say so plainly.\n\nPASSAGE:\n$context"
            aiRouter.run("Ask Damsel", systemPrompt, question)
                .onSuccess { answer ->
                    _state.value = _state.value.copy(ask = _state.value.ask?.copy(answer = answer, isLoading = false))
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        ask = _state.value.ask?.copy(isLoading = false, error = error.message)
                    )
                }
        }
    }

    fun dismissAsk() {
        _state.value = _state.value.copy(ask = null)
    }

    fun speakCurrentPage() {
        val current = _state.value
        val voice = current.selectedVoice ?: return
        val text = current.pages.getOrNull(current.currentPage) ?: return
        _state.value = current.copy(speech = SpeechState.Generating)
        viewModelScope.launch {
            voiceRouter.synthesize(text, voice.id, current.emotion)
                .onSuccess { bytes -> playAudio(bytes) }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        speech = SpeechState.Failed(error.message ?: "Damsel couldn't read that aloud.")
                    )
                }
        }
    }

    fun stopSpeaking() {
        mediaPlayer?.release()
        mediaPlayer = null
        _state.value = _state.value.copy(speech = SpeechState.Idle)
    }

    private fun playAudio(bytes: ByteArray) {
        val file = File.createTempFile("damsel_speech", ".mp3", appContext.cacheDir)
        file.writeBytes(bytes)
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnPreparedListener {
                _state.value = _state.value.copy(speech = SpeechState.Playing)
                start()
            }
            setOnCompletionListener {
                _state.value = _state.value.copy(speech = SpeechState.Idle)
                file.delete()
            }
            setOnErrorListener { _, _, _ ->
                _state.value = _state.value.copy(speech = SpeechState.Failed("Playback failed."))
                true
            }
            prepareAsync()
        }
    }

    override fun onCleared() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onCleared()
    }
}
