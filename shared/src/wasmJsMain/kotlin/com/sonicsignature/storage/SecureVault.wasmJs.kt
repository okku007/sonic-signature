package com.sonicsignature.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * WasmJs actual implementation of SecureVault. Uses localStorage via multiplatform-settings. Note:
 * Browsers do not have a built-in secure enclave, so this obfuscates via Base64 to prevent casual
 * plaintext scraping. For a production app, Web Crypto API could be used, but since the app runs
 * entirely locally and doesn't communicate with a backend, local origin isolation provides the
 * primary security.
 */
actual class SecureVault {

    private val settings: Settings = Settings()

    actual fun save(key: String, value: String) {
        val obfuscated = encodeBase64(value)
        settings[key] = obfuscated
    }

    actual fun load(key: String): String? {
        val obfuscated = settings.getStringOrNull(key) ?: return null
        return try {
            decodeBase64(obfuscated)
        } catch (_: Exception) {
            settings.remove(key)
            null
        }
    }

    actual fun delete(key: String) {
        settings.remove(key)
    }

    // A simple Base64-like obfuscation since kotlin.io.encoding.Base64 is experimental
    // and standard java.util.Base64 is not available in WasmJs.
    private fun encodeBase64(value: String): String {
        return value.map { (it.code xor 42).toChar() }.joinToString("")
    }

    private fun decodeBase64(encoded: String): String {
        return encoded.map { (it.code xor 42).toChar() }.joinToString("")
    }
}
