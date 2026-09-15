package com.damsel.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.damsel.app.data.SecureKeyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val keyStore: SecureKeyStore
) : ViewModel() {

    var maskedKey by mutableStateOf(keyStore.maskedOpenAiKey())
        private set

    fun saveKey(rawKey: String) {
        val trimmed = rawKey.trim()
        if (trimmed.isEmpty()) return
        keyStore.saveOpenAiKey(trimmed)
        maskedKey = keyStore.maskedOpenAiKey()
    }

    fun clearKey() {
        keyStore.clearOpenAiKey()
        maskedKey = null
    }
}
