package com.damsel.app.voice

/** A voice identity the reader can choose, per spec §14 — a product identity, not an engine name. */
data class DamselVoice(val id: String, val displayName: String, val description: String)

/** Emotion presets exposed to the user, per spec §16 — plain concepts, never engine parameters. */
enum class VoiceEmotion(val label: String, val instruction: String) {
    NEUTRAL("Neutral", "Speak in a clear, neutral tone."),
    GENTLE("Gentle", "Speak gently and warmly, at a calm, unhurried pace."),
    DRAMATIC("Dramatic", "Speak with cinematic, dramatic delivery, building tension where the text calls for it."),
    CALM("Calm", "Speak in a calm, soothing, reassuring tone."),
    EXCITED("Excited", "Speak with energy and enthusiasm."),
    SERIOUS("Serious", "Speak in a measured, serious, professional tone.")
}

/**
 * Spec §20: never hardcode a single voice provider. The Reader UI only calls this interface;
 * ElevenLabs/Google/etc. become additional adapters without touching the Voice Studio screen.
 */
interface VoiceProvider {
    val id: String
    fun isConfigured(): Boolean
    suspend fun voices(): List<DamselVoice>
    suspend fun synthesize(text: String, voiceId: String, emotion: VoiceEmotion): Result<ByteArray>
}
