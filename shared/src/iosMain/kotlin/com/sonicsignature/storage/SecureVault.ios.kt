package com.sonicsignature.storage

import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.set

/**
 * iOS actual implementation of SecureVault.
 * Uses KeychainSettings from multiplatform-settings, which delegates to Apple Keychain Services.
 * Keys are isolated by the Secure Enclave on supported devices.
 * Constitution §1.3: Platform-native secure storage.
 */
actual class SecureVault {

    private val settings = KeychainSettings("com.sonicsignature.vault")

    actual fun save(key: String, value: String) {
        settings[key] = value
    }

    actual fun load(key: String): String? {
        return settings.getStringOrNull(key)
    }

    actual fun delete(key: String) {
        settings.remove(key)
    }
}
