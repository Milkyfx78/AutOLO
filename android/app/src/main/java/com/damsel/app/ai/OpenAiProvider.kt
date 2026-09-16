package com.damsel.app.ai

import com.damsel.app.data.SecureKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Real adapter for one provider (spec §24 BYOK). The Router only ever sees [AiProvider]. */
@Singleton
class OpenAiProvider @Inject constructor(
    private val keyStore: SecureKeyStore,
    private val httpClient: OkHttpClient
) : AiProvider {

    override val id = "openai"
    override val displayName = "OpenAI"

    override fun isConfigured(): Boolean = !keyStore.openAiKey().isNullOrBlank()

    override suspend fun complete(systemPrompt: String, userPrompt: String): Result<String> =
        withContext(Dispatchers.IO) {
            val key = keyStore.openAiKey()
                ?: return@withContext Result.failure(NoProviderConfiguredException("Ask Damsel"))

            val body = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", JSONArray().apply {
                    put(JSONObject().put("role", "system").put("content", systemPrompt))
                    put(JSONObject().put("role", "user").put("content", userPrompt))
                })
            }

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $key")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            try {
                httpClient.newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val message = runCatching {
                            JSONObject(raw).getJSONObject("error").getString("message")
                        }.getOrDefault("OpenAI returned ${response.code}.")
                        return@withContext Result.failure(IOException(message))
                    }
                    val text = JSONObject(raw)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    Result.success(text)
                }
            } catch (e: IOException) {
                Result.failure(IOException("Couldn't reach OpenAI — check your connection.", e))
            } catch (e: Exception) {
                Result.failure(IOException("OpenAI sent back something Damsel didn't understand.", e))
            }
        }
}
