package com.damsel.app.voice

import javax.inject.Inject
import javax.inject.Singleton

/** Spec §20/§21: the Reader talks only to this router, never to a concrete [VoiceProvider]. */
@Singleton
class VoiceRouter @Inject constructor(
    openAiVoice: OpenAiVoiceProvider
) {
    private val providers: List<VoiceProvider> = listOf(openAiVoice)

    private fun activeProvider(): VoiceProvider? = providers.firstOrNull { it.isConfigured() }

    suspend fun voices(): List<DamselVoice> = activeProvider()?.voices() ?: emptyList()

    suspend fun synthesize(text: String, voiceId: String, emotion: VoiceEmotion): Result<ByteArray> {
        val provider = activeProvider()
            ?: return Result.failure(IllegalStateException("No voice provider is set up yet. Add an API key in Settings."))
        return provider.synthesize(text, voiceId, emotion)
    }
}
