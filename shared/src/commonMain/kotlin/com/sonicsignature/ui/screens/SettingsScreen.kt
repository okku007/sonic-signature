package com.sonicsignature.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sonicsignature.api.LLMClientFactory
import com.sonicsignature.ui.components.SonicBottomNav
import com.sonicsignature.ui.components.SonicContentColumn
import com.sonicsignature.ui.components.SonicField
import com.sonicsignature.ui.components.SonicPanel
import com.sonicsignature.ui.components.SonicPrimaryButton
import com.sonicsignature.ui.components.SonicScreenHeader
import com.sonicsignature.ui.components.SonicSecondaryButton
import com.sonicsignature.ui.components.SonicStatusMessage
import com.sonicsignature.ui.components.SonicTopLevelDestination
import com.sonicsignature.ui.components.SonicTone
import com.sonicsignature.ui.util.rememberKeyboardVisible
import com.sonicsignature.viewmodel.SettingsUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
        state: SettingsUiState,
        onBack: () -> Unit,
        onNavigateToDiscover: () -> Unit,
        onSave: (provider: LLMClientFactory.Provider, apiKey: String, modelId: String) -> Unit,
        onValidate: (provider: LLMClientFactory.Provider, apiKey: String, modelId: String) -> Unit,
        onValidateLastFmKey: (String) -> Unit,
        onClearAll: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val selectedProvider = LLMClientFactory.Provider.OPEN_ROUTER
    var apiKey by remember { mutableStateOf("") }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var openRouterModelId by remember { mutableStateOf("") }
    var lastFmKey by remember { mutableStateOf("") }
    var lastFmKeyVisible by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isKeyboardVisible = rememberKeyboardVisible()
    LaunchedEffect(state.openRouterModelId) {
        openRouterModelId = state.openRouterModelId
    }
    LaunchedEffect(state.validationSuccess) {
        if (state.validationSuccess == true) {
            apiKey = ""
        }
    }
    LaunchedEffect(state.lastFmValidationSuccess) {
        if (state.lastFmValidationSuccess == true) {
            lastFmKey = ""
        }
    }

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .imePadding(),
            contentAlignment = Alignment.TopCenter
    ) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                                .padding(bottom = if (isKeyboardVisible) 24.dp else 176.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SonicContentColumn {
                SonicScreenHeader(
                        title = "SETTINGS",
                        subtitle =
                                "Validate provider credentials."
                )

                SonicPanel(title = "Setup guide", badge = "Docs") {
                    SonicSecondaryButton(
                            text = "Open setup guide",
                            onClick = {
                                uriHandler.openUri(
                                        "https://github.com/okku007/sonic-signature/blob/main/SETUP.md"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            leading = {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                    )
                }

                SonicPanel(title = "Music data", badge = "Last.fm", emphasized = true) {
                    Text(
                            text = "Free key source: last.fm/api/account/create",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SonicField(
                            value = lastFmKey,
                            onValueChange = { lastFmKey = it },
                            label = "Last.fm API key",
                            placeholder =
                                    if (state.lastFmKeyMasked.isNotEmpty()) {
                                        state.lastFmKeyMasked
                                    } else {
                                        "Enter your Last.fm key"
                                    },
                            visualTransformation =
                                    if (lastFmKeyVisible) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                            modifier = Modifier.settingsBringIntoView(scope),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailing = {
                                androidx.compose.material3.IconButton(
                                        onClick = { lastFmKeyVisible = !lastFmKeyVisible }
                                ) {
                                    Icon(
                                            imageVector =
                                                    if (lastFmKeyVisible) {
                                                        Icons.Default.VisibilityOff
                                                    } else {
                                                        Icons.Default.Visibility
                                                    },
                                            contentDescription =
                                                    if (lastFmKeyVisible) "Hide key" else "Show key"
                                    )
                                }
                            },
                            supportingText =
                                    if (state.lastFmKeyMasked.isNotEmpty()) {
                                        "Stored key detected on this device."
                                    } else {
                                        null
                                    }
                    )

                    LastFmStatus(state)

                    SonicPrimaryButton(
                            text = "Validate and save Last.fm key",
                            onClick = { onValidateLastFmKey(lastFmKey) },
                            enabled = lastFmKey.isNotBlank() && !state.isValidatingLastFm,
                            modifier = Modifier.fillMaxWidth()
                    )
                }

                SonicPanel(title = "AI provider", badge = "OpenRouter", emphasized = true) {
                    Text(
                            text =
                                    "Gemini is deprecated in this app. Use OpenRouter for the active recommendation path and free-model access.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SonicField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = "OpenRouter API key",
                            placeholder =
                                    if (state.apiKeyMasked.isNotEmpty()) {
                                        state.apiKeyMasked
                                    } else {
                                        "Enter your OpenRouter key"
                                    },
                            visualTransformation =
                                    if (apiKeyVisible) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                            modifier = Modifier.settingsBringIntoView(scope),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailing = {
                                androidx.compose.material3.IconButton(
                                        onClick = { apiKeyVisible = !apiKeyVisible }
                                ) {
                                    Icon(
                                            imageVector =
                                                    if (apiKeyVisible) {
                                                        Icons.Default.VisibilityOff
                                                    } else {
                                                        Icons.Default.Visibility
                                                    },
                                            contentDescription =
                                                    if (apiKeyVisible) "Hide key" else "Show key"
                                    )
                                }
                            }
                    )

                    SonicField(
                            value = openRouterModelId,
                            onValueChange = { openRouterModelId = it },
                            label = "OpenRouter model id",
                            placeholder = "e.g. openai/gpt-4o-mini",
                            modifier = Modifier.settingsBringIntoView(scope),
                            supportingText =
                                    "Leave blank to keep the current default if you only need key rotation."
                    )

                    ProviderStatus(state)

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SonicSecondaryButton(
                                text = "Save provider",
                                onClick = { onSave(selectedProvider, "", openRouterModelId) },
                                modifier = Modifier.width(180.dp)
                        )
                        SonicPrimaryButton(
                                text = "Validate and save",
                                onClick = {
                                    if (apiKey.isNotBlank()) {
                                        onValidate(selectedProvider, apiKey, openRouterModelId)
                                    } else {
                                        onSave(selectedProvider, "", openRouterModelId)
                                    }
                                },
                                enabled = !state.isValidating,
                                modifier = Modifier.width(220.dp)
                        )
                    }
                }

                SonicPanel(title = "Privacy", badge = "Danger", badgeTone = SonicTone.Danger) {
                    Text(
                            text =
                                    "Clear all stored provider and Last.fm credentials from this device.",
                            style = MaterialTheme.typography.bodyLarge
                    )
                    SonicSecondaryButton(
                            text = "Clear stored data",
                            onClick = { showClearDialog = true },
                            modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Box(
                modifier =
                        Modifier.align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                        visible = !isKeyboardVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 })
                ) {
                    SonicBottomNav(
                            selected = SonicTopLevelDestination.Settings,
                            onDiscoverClick = onNavigateToDiscover,
                            onSettingsClick = {}
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear all stored data?") },
                text = {
                    Text(
                            "This removes the saved Last.fm key, provider key, provider selection, and OpenRouter model id from local storage."
                    )
                },
                confirmButton = {
                    TextButton(
                            onClick = {
                                onClearAll()
                                showClearDialog = false
                            }
                    ) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
                }
                )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.settingsBringIntoView(
        scope: kotlinx.coroutines.CoroutineScope
): Modifier {
    val bringIntoViewRequester = BringIntoViewRequester()
    return this.bringIntoViewRequester(bringIntoViewRequester).onFocusChanged { state ->
        if (state.isFocused) {
            scope.launch {
                delay(250)
                bringIntoViewRequester.bringIntoView()
                delay(250)
                bringIntoViewRequester.bringIntoView()
            }
        }
    }
}

@Composable
private fun LastFmStatus(state: SettingsUiState) {
    when {
        state.isValidatingLastFm ->
                SonicStatusMessage(
                        title = "Validating",
                        body = "Checking Last.fm access against the current key.",
                        tone = SonicTone.Accent
                )
        state.lastFmValidationSuccess == true ->
                SonicStatusMessage(
                        title = "Stored",
                        body = "Last.fm key validated and saved successfully.",
                        tone = SonicTone.Success
                )
        state.lastFmValidationSuccess == false ->
                SonicStatusMessage(
                        title = "Validation failed",
                        body = state.lastFmError ?: "Last.fm validation failed.",
                        tone = SonicTone.Danger
                )
        state.lastFmKeyMasked.isNotEmpty() ->
                SonicStatusMessage(
                        title = "Saved key detected",
                        body = "Masked key present: ${state.lastFmKeyMasked}",
                        tone = SonicTone.Neutral
                )
    }
}

@Composable
private fun ProviderStatus(state: SettingsUiState) {
    when {
        state.isValidating ->
                SonicStatusMessage(
                        title = "Validating",
                        body = "Running a provider completion probe with the entered key.",
                        tone = SonicTone.Accent
                )
        state.validationSuccess == true ->
                SonicStatusMessage(
                        title = "Stored",
                        body =
                                if (state.apiKeyMasked.isNotEmpty()) {
                                    "Provider key validated and stored as ${state.apiKeyMasked}."
                                } else {
                                    "Provider settings saved."
                                },
                        tone = SonicTone.Success
                )
        state.validationSuccess == false ->
                SonicStatusMessage(
                        title = "Validation failed",
                        body = state.error ?: "Provider validation failed.",
                        tone = SonicTone.Danger
                )
        state.apiKeyMasked.isNotEmpty() ->
                SonicStatusMessage(
                        title = "Saved key detected",
                        body = "Masked key present: ${state.apiKeyMasked}",
                        tone = SonicTone.Neutral
                )
    }
}
