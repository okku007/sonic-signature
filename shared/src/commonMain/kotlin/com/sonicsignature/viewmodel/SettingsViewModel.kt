package com.sonicsignature.viewmodel

import com.sonicsignature.api.GeminiProvider
import com.sonicsignature.api.LLMClientFactory
import com.sonicsignature.api.OpenRouterProvider
import com.sonicsignature.storage.SecureVault
import com.sonicsignature.storage.VaultKeys
import io.ktor.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
        val llmProvider: LLMClientFactory.Provider? = null,
        val apiKeyMasked: String = "",
        val openRouterModelId: String = "",
        val isValidating: Boolean = false,
        val validationSuccess: Boolean? = null, // null = not yet validated
        val error: String? = null
)

/**
 * ViewModel for the Settings screen. Reads/writes to SecureVault. Never logs or exposes raw API
 * keys. Constitution §1.2: Keys never logged. Constitution §2.4: Provider shown to user.
 */
class SettingsViewModel(
        private val vault: SecureVault,
        private val httpClient: HttpClient,
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        loadCurrentSettings()
    }

    private fun loadCurrentSettings() {
        val providerName = vault.load(VaultKeys.LLM_PROVIDER)
        val provider =
                providerName?.let {
                    runCatching { LLMClientFactory.Provider.valueOf(it) }.getOrNull()
                }
        val hasKey = vault.load(VaultKeys.LLM_API_KEY) != null

        _state.value =
                SettingsUiState(
                        llmProvider = provider,
                        apiKeyMasked = if (hasKey) "••••••••••••" else "",
                        openRouterModelId = vault.load(VaultKeys.OPENROUTER_MODEL_ID) ?: ""
                )
    }

    fun saveSettings(
            provider: LLMClientFactory.Provider,
            apiKey: String,
            openRouterModelId: String = ""
    ) {
        vault.save(VaultKeys.LLM_PROVIDER, provider.name)
        if (apiKey.isNotBlank()) vault.save(VaultKeys.LLM_API_KEY, apiKey)
        saveOrClear(VaultKeys.OPENROUTER_MODEL_ID, openRouterModelId)

        loadCurrentSettings()
    }

    private fun saveOrClear(key: String, value: String) {
        if (value.isNotBlank()) vault.save(key, value) else vault.delete(key)
    }

    fun validateApiKey(provider: LLMClientFactory.Provider, apiKey: String, modelId: String = "") {
        _state.value =
                _state.value.copy(isValidating = true, validationSuccess = null, error = null)

        scope.launch {
            try {
                val testPrompt = "Reply with only the word: OK"
                val llmProvider =
                        when (provider) {
                            LLMClientFactory.Provider.GEMINI -> GeminiProvider(httpClient, apiKey)
                            LLMClientFactory.Provider.OPEN_ROUTER ->
                                    OpenRouterProvider(
                                            httpClient,
                                            apiKey,
                                            modelId.ifBlank { "openai/gpt-4o-mini" }
                                    )
                        }
                llmProvider.complete(testPrompt)
                _state.value = _state.value.copy(isValidating = false, validationSuccess = true)
            } catch (e: Exception) {
                _state.value =
                        _state.value.copy(
                                isValidating = false,
                                validationSuccess = false,
                                error = e.message ?: "Validation failed. Check your API key."
                        )
            }
        }
    }

    fun clearAllData() {
        VaultKeys.run {
            listOf(LLM_PROVIDER, LLM_API_KEY, OPENROUTER_MODEL_ID).forEach { vault.delete(it) }
        }
        _state.value = SettingsUiState()
    }

    fun dispose() {
        scope.cancel()
    }
}
