package com.sonicsignature.viewmodel

import com.sonicsignature.api.LLMClientFactory
import com.sonicsignature.api.OpenRouterProvider
import com.sonicsignature.storage.SecureVault
import com.sonicsignature.storage.VaultKeys
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
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
        val error: String? = null,
        // Last.fm key state
        val lastFmKeyMasked: String = "",
        val isValidatingLastFm: Boolean = false,
        val lastFmValidationSuccess: Boolean? = null,
        val lastFmError: String? = null
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

    private fun normalizeProvider(providerName: String?): LLMClientFactory.Provider? =
            when (runCatching { providerName?.let(LLMClientFactory.Provider::valueOf) }.getOrNull()) {
                LLMClientFactory.Provider.OPEN_ROUTER -> LLMClientFactory.Provider.OPEN_ROUTER
                LLMClientFactory.Provider.GEMINI, null -> null
            }

    private fun loadCurrentSettings() {
        val providerName = vault.load(VaultKeys.LLM_PROVIDER)
        val provider = normalizeProvider(providerName)
        val hasLlmKey = provider == LLMClientFactory.Provider.OPEN_ROUTER &&
                vault.load(VaultKeys.LLM_API_KEY) != null
        val hasLastFmKey = vault.load(VaultKeys.LASTFM_API_KEY) != null

        _state.value =
                SettingsUiState(
                        llmProvider = provider,
                        apiKeyMasked = if (hasLlmKey) "••••••••••••" else "",
                        openRouterModelId = vault.load(VaultKeys.OPENROUTER_MODEL_ID) ?: "",
                        lastFmKeyMasked = if (hasLastFmKey) "••••••••••••" else ""
                )
    }

    fun saveSettings(
            provider: LLMClientFactory.Provider,
            apiKey: String,
            openRouterModelId: String = ""
    ) {
        val currentProvider = normalizeProvider(vault.load(VaultKeys.LLM_PROVIDER))
        vault.save(VaultKeys.LLM_PROVIDER, LLMClientFactory.Provider.OPEN_ROUTER.name)
        if (apiKey.isNotBlank()) {
            vault.save(VaultKeys.LLM_API_KEY, apiKey)
        } else if (currentProvider != LLMClientFactory.Provider.OPEN_ROUTER) {
            vault.delete(VaultKeys.LLM_API_KEY)
        }
        saveOrClear(VaultKeys.OPENROUTER_MODEL_ID, openRouterModelId)

        loadCurrentSettings()
    }

    // ── Last.fm key management ────────────────────────────────────────────────

    fun saveLastFmKey(key: String) {
        if (key.isNotBlank()) vault.save(VaultKeys.LASTFM_API_KEY, key)
        loadCurrentSettings()
    }

    fun validateLastFmKey(key: String) {
        _state.value =
                _state.value.copy(
                        isValidatingLastFm = true,
                        lastFmValidationSuccess = null,
                        lastFmError = null
                )

        scope.launch {
            try {
                val response =
                        httpClient.get("https://ws.audioscrobbler.com/2.0/") {
                            parameter("method", "track.search")
                            parameter("track", "test")
                            parameter("api_key", key)
                            parameter("format", "json")
                            parameter("limit", 1)
                        }

                if (response.status.isSuccess()) {
                    // Save key on successful validation
                    vault.save(VaultKeys.LASTFM_API_KEY, key)
                    loadCurrentSettings()
                    _state.value =
                            _state.value.copy(
                                    isValidatingLastFm = false,
                                    lastFmValidationSuccess = true
                            )
                } else {
                    _state.value =
                            _state.value.copy(
                                    isValidatingLastFm = false,
                                    lastFmValidationSuccess = false,
                                    lastFmError =
                                            "Invalid API key. Check your key at last.fm/api/account/create"
                            )
                }
            } catch (e: Exception) {
                _state.value =
                        _state.value.copy(
                                isValidatingLastFm = false,
                                lastFmValidationSuccess = false,
                                lastFmError = e.message
                                                ?: "Validation failed. Check your network connection."
                        )
            }
        }
    }

    // ── LLM key management ────────────────────────────────────────────────────

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
                        OpenRouterProvider(
                                httpClient,
                                apiKey,
                                modelId.ifBlank { "openai/gpt-4o-mini" }
                        )
                llmProvider.complete(testPrompt)
                // Save the key immediately on successful validation so the Home screen
                // button updates reactively as soon as the user navigates back.
                vault.save(VaultKeys.LLM_PROVIDER, LLMClientFactory.Provider.OPEN_ROUTER.name)
                vault.save(VaultKeys.LLM_API_KEY, apiKey)
                if (modelId.isNotBlank()) vault.save(VaultKeys.OPENROUTER_MODEL_ID, modelId)
                loadCurrentSettings()
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
            listOf(LLM_PROVIDER, LLM_API_KEY, OPENROUTER_MODEL_ID, LASTFM_API_KEY).forEach {
                vault.delete(it)
            }
        }
        _state.value = SettingsUiState()
    }

    fun dispose() {
        scope.cancel()
    }
}
