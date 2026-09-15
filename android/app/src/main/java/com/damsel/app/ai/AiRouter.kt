package com.damsel.app.ai

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Spec §21/§23: every AI call goes through the router, never straight to a provider. The
 * vertical slice registers one adapter (OpenAI); adding Anthropic/Gemini later means adding
 * them to [providers], not touching any screen.
 */
@Singleton
class AiRouter @Inject constructor(
    private val openAi: OpenAiProvider
) {
    private val providers: List<AiProvider> = listOf(openAi)

    fun providerFor(task: String): AiProvider? = providers.firstOrNull { it.isConfigured() }

    suspend fun run(task: String, systemPrompt: String, userPrompt: String): Result<String> {
        val provider = providerFor(task)
            ?: return Result.failure(NoProviderConfiguredException(task))
        return provider.complete(systemPrompt, userPrompt)
    }
}
