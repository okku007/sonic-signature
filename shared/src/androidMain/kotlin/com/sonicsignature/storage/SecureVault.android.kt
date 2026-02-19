package com.sonicsignature.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Android actual implementation of SecureVault.
 * Uses EncryptedSharedPreferences backed by Android KeyStore (hardware-backed on supported devices).
 * Constitution §1.3: Platform-native secure storage, no plaintext fallback.
 */
actual class SecureVault {

    private val prefs by lazy {
        val context = ApplicationContextHolder.context
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "sonic_signature_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    actual fun save(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun load(key: String): String? {
        return prefs.getString(key, null)
    }

    actual fun delete(key: String) {
        prefs.edit().remove(key).apply()
    }
}
