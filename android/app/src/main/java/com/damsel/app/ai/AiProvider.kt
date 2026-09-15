package com.damsel.app.ai

/**
 * Every AI text provider (OpenAI, Anthropic, Gemini, ...) implements this same shape, per
 * spec §20/§78 — the AI Router and every screen depend only on this interface, never on a
 * concrete provider, so a new one is a new adapter, not a UI change.
 */
interface AiProvider {
    val id: String
    val displayName: String
    fun isConfigured(): Boolean
    suspend fun complete(systemPrompt: String, userPrompt: String): Result<String>
}

/** Thrown/returned as a Result failure when no provider is configured for a task — spec Rule 1: no fake functionality. */
class NoProviderConfiguredException(val task: String) :
    Exception("No AI provider is set up for \"$task\" yet. Add an API key in Settings → AI & Providers.")
