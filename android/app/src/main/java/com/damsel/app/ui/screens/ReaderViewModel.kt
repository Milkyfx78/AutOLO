package com.damsel.app.ui.screens

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damsel.app.ai.AiRouter
import com.damsel.app.data.BookDao
import com.damsel.app.data.HighlightColor
import com.damsel.app.data.HighlightEntity
import com.damsel.app.data.ReadingProgressEntity
import com.damsel.app.voice.DamselVoice
import com.damsel.app.voice.VoiceEmotion
import com.damsel.app.voice.VoiceRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class AskExchange(val question: String, val answer: String?, val isLoading: Boolean, val error: String? = null)

sealed interface SpeechState {
    data object Idle : SpeechState
    data object Generating : SpeechState
    data object Playing : SpeechState
    data object Paused : SpeechState
    data class Failed(val message: String) : SpeechState
}

/** Playback speeds offered in the player — spec's "Speaking Speed" concept, kept to plain numbers. */
val PLAYBACK_SPEEDS = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)

data class ReaderUiState(
    val title: String = "",
    val pages: List<String> = emptyList(),
    val currentPage: Int = 0,
    val isLoading: Boolean = true,
    val selectedVoice: DamselVoice? = null,
    val voices: List<DamselVoice> = emptyList(),
    val emotion: VoiceEmotion = VoiceEmotion.NEUTRAL,
    val speech: SpeechState = SpeechState.Idle,
    val playbackSpeed: Float = 1f,
    val playbackPositionMs: Int = 0,
    val playbackDurationMs: Int = 0,
    val sleepTimerMinutesRemaining: Int? = null,
    val highlights: List<HighlightEntity> = emptyList(),
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
    private var highlightsJob: Job? = null
    private var positionPollJob: Job? = null
    private var sleepTimerJob: Job? = null

    fun load(id: String) {
        if (bookId == id) return
        bookId = id
        viewModelScope.launch {
            try {
                val book = bookDao.getBook(id)
                val pages = bookDao.getPages(id).map { it.text }
                val progress = bookDao.getProgress(id)
                val voices = runCatching { voiceRouter.voices() }.getOrDefault(emptyList())
                if (book == null) {
                    _state.value = _state.value.copy(isLoading = false, loadError = "This book is no longer in your library.")
                    return@launch
                }
                val startPage = (progress?.lastPageIndex ?: 0).coerceIn(0, (pages.size - 1).coerceAtLeast(0))
                _state.value = _state.value.copy(
                    title = book.title,
                    pages = pages,
                    currentPage = startPage,
                    isLoading = false,
                    voices = voices,
                    selectedVoice = voices.firstOrNull()
                )
                observeHighlightsForCurrentPage()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    loadError = "Damsel couldn't open this book. Try removing and re-adding it."
                )
            }
        }
    }

    private fun observeHighlightsForCurrentPage() {
        val id = bookId ?: return
        highlightsJob?.cancel()
        highlightsJob = viewModelScope.launch {
            bookDao.observeHighlights(id, _state.value.currentPage).collect { highlights ->
                _state.value = _state.value.copy(highlights = highlights)
            }
        }
    }

    fun goToPage(index: Int) {
        val pages = _state.value.pages
        if (index !in pages.indices) return
        _state.value = _state.value.copy(currentPage = index, ask = null, highlights = emptyList())
        persistProgress(index)
        observeHighlightsForCurrentPage()
    }

    private fun persistProgress(index: Int) {
        val id = bookId ?: return
        viewModelScope.launch {
            bookDao.upsertProgress(ReadingProgressEntity(id, index, System.currentTimeMillis()))
        }
    }

    fun setGenre(genre: String?) {
        val id = bookId ?: return
        viewModelScope.launch { bookDao.setGenre(id, genre) }
    }

    fun selectVoice(voice: DamselVoice) {
        _state.value = _state.value.copy(selectedVoice = voice)
    }

    fun selectEmotion(emotion: VoiceEmotion) {
        _state.value = _state.value.copy(emotion = emotion)
    }

    // ---------------- Highlights (spec §38) ----------------

    fun addHighlight(startOffset: Int, endOffset: Int, quotedText: String, color: HighlightColor, note: String?) {
        val id = bookId ?: return
        if (quotedText.isBlank() || startOffset >= endOffset) return
        viewModelScope.launch {
            bookDao.insertHighlight(
                HighlightEntity(
                    id = UUID.randomUUID().toString(),
                    bookId = id,
                    pageIndex = _state.value.currentPage,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    quotedText = quotedText,
                    color = color.name,
                    note = note?.takeIf { it.isNotBlank() },
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteHighlight(highlightId: String) {
        viewModelScope.launch { bookDao.deleteHighlight(highlightId) }
    }

    // ---------------- Ask Damsel ----------------

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

    // ---------------- Voice playback ----------------

    fun speakCurrentPage() {
        val current = _state.value
        val voice = current.selectedVoice ?: return
        val text = current.pages.getOrNull(current.currentPage) ?: return
        _state.value = current.copy(speech = SpeechState.Generating)
        viewModelScope.launch {
            val result = try {
                voiceRouter.synthesize(text, voice.id, current.emotion)
            } catch (e: Exception) {
                Result.failure(e)
            }
            result
                .onSuccess { bytes -> playAudio(bytes) }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        speech = SpeechState.Failed(error.message ?: "Damsel couldn't read that aloud.")
                    )
                }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        when (_state.value.speech) {
            is SpeechState.Playing -> {
                runCatching { player.pause() }
                _state.value = _state.value.copy(speech = SpeechState.Paused)
            }
            is SpeechState.Paused -> {
                runCatching { player.start() }
                _state.value = _state.value.copy(speech = SpeechState.Playing)
                startPositionPolling()
            }
            else -> Unit
        }
    }

    fun stopSpeaking() {
        positionPollJob?.cancel()
        sleepTimerJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        _state.value = _state.value.copy(
            speech = SpeechState.Idle,
            playbackPositionMs = 0,
            playbackDurationMs = 0,
            sleepTimerMinutesRemaining = null
        )
    }

    fun setPlaybackSpeed(speed: Float) {
        _state.value = _state.value.copy(playbackSpeed = speed)
        val player = mediaPlayer ?: return
        val speech = _state.value.speech
        if (speech !is SpeechState.Playing && speech !is SpeechState.Paused) return
        runCatching { player.playbackParams = PlaybackParams().setSpeed(speed) }
    }

    fun seekTo(positionMs: Int) {
        val player = mediaPlayer ?: return
        runCatching { player.seekTo(positionMs) }
        _state.value = _state.value.copy(playbackPositionMs = positionMs)
    }

    fun skip(deltaMs: Int) {
        val player = mediaPlayer ?: return
        val target = (player.currentPosition + deltaMs).coerceIn(0, player.duration.coerceAtLeast(0))
        seekTo(target)
    }

    /** Stops narration automatically after [minutes] — spec §12 sleep timer. Pass null to cancel. */
    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null) {
            _state.value = _state.value.copy(sleepTimerMinutesRemaining = null)
            return
        }
        _state.value = _state.value.copy(sleepTimerMinutesRemaining = minutes)
        sleepTimerJob = viewModelScope.launch {
            var remaining = minutes * 60
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _state.value = _state.value.copy(sleepTimerMinutesRemaining = (remaining + 59) / 60)
            }
            stopSpeaking()
        }
    }

    private fun startPositionPolling() {
        positionPollJob?.cancel()
        positionPollJob = viewModelScope.launch {
            while (true) {
                val player = mediaPlayer
                if (player != null && _state.value.speech is SpeechState.Playing) {
                    val position = runCatching { player.currentPosition }.getOrDefault(0)
                    val duration = runCatching { player.duration }.getOrDefault(0).coerceAtLeast(0)
                    _state.value = _state.value.copy(playbackPositionMs = position, playbackDurationMs = duration)
                }
                delay(300)
            }
        }
    }

    /**
     * This used to crash the whole app (not just show an error) whenever MediaPlayer rejected
     * the audio — e.g. an empty or malformed response body — because setDataSource()/prepareAsync()
     * were called with no try/catch inside a coroutine, so the exception had nowhere safe to land.
     * Everything here is now guarded so a bad audio segment ends in SpeechState.Failed, never a crash.
     */
    private fun playAudio(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            _state.value = _state.value.copy(
                speech = SpeechState.Failed("Damsel didn't get any audio back for that page. Try again.")
            )
            return
        }

        var file: File? = null
        try {
            file = File.createTempFile("damsel_speech", ".mp3", appContext.cacheDir)
            file.writeBytes(bytes)

            mediaPlayer?.release()
            val tempFile = file
            val speed = _state.value.playbackSpeed
            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener {
                    if (speed != 1f) runCatching { playbackParams = PlaybackParams().setSpeed(speed) }
                    _state.value = _state.value.copy(speech = SpeechState.Playing, playbackDurationMs = duration)
                    start()
                    startPositionPolling()
                }
                setOnCompletionListener {
                    positionPollJob?.cancel()
                    _state.value = _state.value.copy(speech = SpeechState.Idle, playbackPositionMs = 0)
                    tempFile.delete()
                }
                setOnErrorListener { _, _, _ ->
                    positionPollJob?.cancel()
                    _state.value = _state.value.copy(
                        speech = SpeechState.Failed("Damsel couldn't play that back. Try again.")
                    )
                    tempFile.delete()
                    true
                }
                setDataSource(tempFile.absolutePath)
                prepareAsync()
            }
        } catch (e: Exception) {
            file?.delete()
            mediaPlayer?.release()
            mediaPlayer = null
            _state.value = _state.value.copy(
                speech = SpeechState.Failed("Damsel couldn't play that back. Try again.")
            )
        }
    }

    override fun onCleared() {
        positionPollJob?.cancel()
        sleepTimerJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onCleared()
    }
}
