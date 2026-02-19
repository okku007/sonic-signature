package com.sonicsignature.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * Desktop actual implementation of SecureVault.
 * Uses multiplatform-settings backed by JVM Preferences (user-scoped, OS-managed).
 * Note: Full OS keychain integration (libsecret/DPAPI) is deferred to v1.1 (T-202).
 * Constitution §1.3: No plaintext file storage; JVM Preferences are OS-user-scoped.
 */
actual class SecureVault {

    private val settings: Settings = Settings()

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
