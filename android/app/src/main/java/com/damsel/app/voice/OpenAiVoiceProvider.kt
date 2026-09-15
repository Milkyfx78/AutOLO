package com.damsel.app.voice

import com.damsel.app.data.SecureKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Real cloud voice adapter using the same BYOK OpenAI key as [com.damsel.app.ai.OpenAiProvider]. */
@Singleton
class OpenAiVoiceProvider @Inject constructor(
    private val keyStore: SecureKeyStore,
    private val httpClient: OkHttpClient
) : VoiceProvider {

    override val id = "openai-voice"
    override fun isConfigured(): Boolean = !keyStore.openAiKey().isNullOrBlank()

    override suspend fun voices(): List<DamselVoice> = listOf(
        DamselVoice("alloy", "Damsel", "Warm, balanced, the default companion voice."),
        DamselVoice("onyx", "Merlin", "Deep and resonant — cinematic narration."),
        DamselVoice("nova", "Aurora", "Bright and expressive."),
        DamselVoice("shimmer", "Luna", "Soft and gentle."),
        DamselVoice("echo", "Sage", "Calm and measured."),
        DamselVoice("fable", "Iris", "Warm storyteller tone.")
    )

    override suspend fun synthesize(
        text: String,
        voiceId: String,
        emotion: VoiceEmotion
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        val key = keyStore.openAiKey()
            ?: return@withContext Result.failure(IllegalStateException("No voice provider is set up yet."))

        val body = JSONObject().apply {
            put("model", "gpt-4o-mini-tts")
            put("voice", voiceId)
            put("input", text.take(4000))
            put("instructions", emotion.instruction)
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/audio/speech")
            .addHeader("Authorization", "Bearer $key")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val raw = response.body?.string().orEmpty()
                    val message = runCatching {
                        JSONObject(raw).getJSONObject("error").getString("message")
                    }.getOrDefault("Damsel couldn't generate that voice segment (HTTP ${response.code}).")
                    return@withContext Result.failure(IOException(message))
                }
                Result.success(response.body?.bytes() ?: ByteArray(0))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Couldn't reach the voice service — check your connection.", e))
        }
    }
}
