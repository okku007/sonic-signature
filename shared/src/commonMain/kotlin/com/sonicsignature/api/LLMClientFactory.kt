package com.sonicsignature.api

import com.sonicsignature.storage.SecureVault
import com.sonicsignature.storage.VaultKeys
import io.ktor.client.*

/**
 * Reads the user's configured LLM provider from SecureVault and returns
 * the appropriate LLMProvider implementation.
 * Constitution §2.3: Provider-neutral — no default or preferred provider.
 * Constitution §2.4: Returns null if no provider configured (caller shows onboarding).
 */
class LLMClientFactory(
    private val httpClient: HttpClient,
    private val vault: SecureVault
) {
    enum class Provider { GEMINI, OPEN_ROUTER }

    fun create(): LLMProvider? {
        val providerName = vault.load(VaultKeys.LLM_PROVIDER) ?: return null
        val apiKey = vault.load(VaultKeys.LLM_API_KEY) ?: return null

        return when (runCatching { Provider.valueOf(providerName) }.getOrNull()) {
            Provider.GEMINI -> GeminiProvider(httpClient, apiKey)
            Provider.OPEN_ROUTER -> {
                val modelId = vault.load(VaultKeys.OPENROUTER_MODEL_ID)
                    ?: "openai/gpt-4o-mini" // Sensible fallback model
                OpenRouterProvider(httpClient, apiKey, modelId)
            }
            null -> null
        }
    }
}
