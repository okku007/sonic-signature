package com.sonicsignature.storage

/**
 * Centralized key constants for SecureVault. Constitution §1.2: All API keys stored under these
 * constants, never as ad-hoc strings.
 */
object VaultKeys {
    const val LLM_PROVIDER = "llm_provider"
    const val LLM_API_KEY = "llm_api_key"
    const val OPENROUTER_MODEL_ID = "openrouter_model_id"
}
