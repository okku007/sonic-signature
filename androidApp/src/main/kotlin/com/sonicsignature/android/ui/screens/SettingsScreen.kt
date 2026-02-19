package com.sonicsignature.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sonicsignature.api.LLMClientFactory
import com.sonicsignature.viewmodel.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
        state: SettingsUiState,
        onBack: () -> Unit,
        onSave: (provider: LLMClientFactory.Provider, apiKey: String, modelId: String) -> Unit,
        onValidate: (provider: LLMClientFactory.Provider, apiKey: String, modelId: String) -> Unit,
        onClearAll: () -> Unit
) {
        var selectedProvider by remember {
                mutableStateOf(state.llmProvider ?: LLMClientFactory.Provider.GEMINI)
        }
        var apiKey by remember { mutableStateOf("") }
        var apiKeyVisible by remember { mutableStateOf(false) }
        var openRouterModelId by remember { mutableStateOf(state.openRouterModelId) }
        var showClearDialog by remember { mutableStateOf(false) }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("Settings") },
                                navigationIcon = {
                                        IconButton(onClick = onBack) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                )
                                        }
                                }
                        )
                }
        ) { padding ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(padding)
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                        // ── LLM Provider ─────────────────────────────────────────────────
                        Text("AI Provider", style = MaterialTheme.typography.titleMedium)

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                LLMClientFactory.Provider.entries.forEachIndexed { index, provider
                                        ->
                                        SegmentedButton(
                                                selected = selectedProvider == provider,
                                                onClick = { selectedProvider = provider },
                                                shape =
                                                        SegmentedButtonDefaults.itemShape(
                                                                index,
                                                                LLMClientFactory.Provider.entries
                                                                        .size
                                                        ),
                                                label = {
                                                        Text(
                                                                when (provider) {
                                                                        LLMClientFactory.Provider
                                                                                .GEMINI -> "Gemini"
                                                                        LLMClientFactory.Provider
                                                                                .OPEN_ROUTER ->
                                                                                "OpenRouter"
                                                                }
                                                        )
                                                }
                                        )
                                }
                        }

                        // ── API Key ──────────────────────────────────────────────────────
                        OutlinedTextField(
                                value = apiKey,
                                onValueChange = { apiKey = it },
                                label = { Text("API Key") },
                                placeholder = {
                                        Text(
                                                if (state.apiKeyMasked.isNotEmpty())
                                                        state.apiKeyMasked
                                                else "Enter your API key"
                                        )
                                },
                                visualTransformation =
                                        if (apiKeyVisible) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                trailingIcon = {
                                        IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                                Icon(
                                                        if (apiKeyVisible)
                                                                Icons.Default.VisibilityOff
                                                        else Icons.Default.Visibility,
                                                        contentDescription =
                                                                if (apiKeyVisible) "Hide key"
                                                                else "Show key"
                                                )
                                        }
                                },
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                        )

                        // ── OpenRouter Model ID (conditional) ────────────────────────────
                        if (selectedProvider == LLMClientFactory.Provider.OPEN_ROUTER) {
                                OutlinedTextField(
                                        value = openRouterModelId,
                                        onValueChange = { openRouterModelId = it },
                                        label = { Text("Model ID") },
                                        placeholder = { Text("e.g. openai/gpt-4o-mini") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )
                        }

                        // ── Validation status ────────────────────────────────────────────
                        when {
                                state.isValidating ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(
                                                        modifier = Modifier.size(14.dp),
                                                        strokeWidth = 2.dp,
                                                        color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                        "Validating API key…",
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        style = MaterialTheme.typography.bodySmall
                                                )
                                        }
                                state.validationSuccess == true -> {
                                        Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors =
                                                        CardDefaults.cardColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primaryContainer
                                                        )
                                        ) {
                                                Row(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                Icons.Default.CheckCircle,
                                                                contentDescription = null,
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                        Column {
                                                                Text(
                                                                        "API key validated & saved",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .bodyMedium,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onPrimaryContainer
                                                                )
                                                                if (state.apiKeyMasked.isNotEmpty()
                                                                ) {
                                                                        Text(
                                                                                "Stored: ${state.apiKeyMasked}",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .bodySmall,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onPrimaryContainer
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.7f
                                                                                                )
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                        // Clear the input field after successful save
                                        LaunchedEffect(Unit) { apiKey = "" }
                                }
                                state.validationSuccess == false ->
                                        Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors =
                                                        CardDefaults.cardColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .errorContainer
                                                        )
                                        ) {
                                                Text(
                                                        state.error ?: "Validation failed",
                                                        modifier = Modifier.padding(12.dp),
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onErrorContainer,
                                                        style = MaterialTheme.typography.bodySmall
                                                )
                                        }
                                // Show saved key indicator when there's a stored key but no active
                                // validation
                                state.apiKeyMasked.isNotEmpty() ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                        "Key saved: ${state.apiKeyMasked}",
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        style = MaterialTheme.typography.bodySmall
                                                )
                                        }
                        }
                        // ── Validate & Save ──────────────────────────────────────────────
                        Button(
                                onClick = {
                                        if (apiKey.isNotBlank()) {
                                                onValidate(
                                                        selectedProvider,
                                                        apiKey,
                                                        openRouterModelId
                                                )
                                        }
                                        onSave(selectedProvider, "", openRouterModelId)
                                },
                                enabled = !state.isValidating,
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                if (state.isValidating) {
                                        CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                }
                                Text("Validate & Save")
                        }

                        // Auto-save API key once validation succeeds
                        LaunchedEffect(state.validationSuccess) {
                                if (state.validationSuccess == true && apiKey.isNotBlank()) {
                                        onSave(selectedProvider, apiKey, openRouterModelId)
                                }
                        }

                        HorizontalDivider()

                        // ── Danger Zone ──────────────────────────────────────────────────
                        Text("Privacy", style = MaterialTheme.typography.titleMedium)
                        OutlinedButton(
                                onClick = { showClearDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                        )
                        ) { Text("Clear All Stored Data") }
                }
        }

        if (showClearDialog) {
                AlertDialog(
                        onDismissRequest = { showClearDialog = false },
                        modifier = Modifier,
                        title = { Text("Clear All Data?") },
                        text = {
                                Text(
                                        "This will delete all stored API keys from this device. This cannot be undone."
                                )
                        },
                        confirmButton = {
                                TextButton(
                                        onClick = {
                                                onClearAll()
                                                showClearDialog = false
                                        }
                                ) { Text("Clear", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
                        }
                )
        }
}
