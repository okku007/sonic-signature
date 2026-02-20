package com.sonicsignature.api

/**
 * Unified interface for all LLM providers.
 * Constitution §2.1: No hard-coded API keys. Constitution §2.2: No proxying.
 * All implementations call the provider's API directly from the user's device.
 */
interface LLMProvider {
    suspend fun complete(prompt: String): String
}
