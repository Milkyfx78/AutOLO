package com.damsel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    var draftKey by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI & Providers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxWidth().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Bring your own key. It's encrypted on this device and sent straight to OpenAI — never to a Damsel server.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("OpenAI", style = MaterialTheme.typography.titleLarge)
            Text(
                text = viewModel.maskedKey ?: "Not connected",
                style = MaterialTheme.typography.bodyMedium,
                color = if (viewModel.maskedKey != null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = draftKey,
                onValueChange = { draftKey = it },
                label = { Text("Paste API key") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.saveKey(draftKey); draftKey = "" },
                enabled = draftKey.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save key") }

            if (viewModel.maskedKey != null) {
                OutlinedButton(onClick = viewModel::clearKey, modifier = Modifier.fillMaxWidth()) {
                    Text("Remove key")
                }
            }

            Text(
                "This powers Ask Damsel and cloud voice narration. Other providers (Anthropic, Gemini, ElevenLabs) plug into the same AI Router — see docs/damsel-master-spec.md §24.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
