package com.damsel.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BYOK key storage per spec §25: keys live in Android Keystore-backed encrypted prefs,
 * never in plain Room tables, never logged. [maskedOpenAiKey] is the only form a UI may show
 * once a key is saved.
 */
@Singleton
class SecureKeyStore @Inject constructor(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "damsel_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveOpenAiKey(key: String) = prefs.edit().putString(KEY_OPENAI, key).apply()

    fun openAiKey(): String? = prefs.getString(KEY_OPENAI, null)

    fun clearOpenAiKey() = prefs.edit().remove(KEY_OPENAI).apply()

    fun maskedOpenAiKey(): String? {
        val key = openAiKey() ?: return null
        if (key.length <= 6) return "••••••"
        return "${key.take(3)}${"•".repeat(8)}${key.takeLast(4)}"
    }

    private companion object {
        const val KEY_OPENAI = "openai_api_key"
    }
}
