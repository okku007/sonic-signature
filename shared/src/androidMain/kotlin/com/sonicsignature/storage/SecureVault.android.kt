package com.sonicsignature.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android actual implementation of SecureVault.
 * Encrypts values with an AES-GCM key stored in Android Keystore.
 * Constitution §1.3: Platform-native secure storage, no plaintext fallback.
 */
actual class SecureVault {

    private val prefs by lazy {
        ApplicationContextHolder.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val secretKey by lazy { getOrCreateSecretKey() }

    actual fun save(key: String, value: String) {
        prefs.edit().putString(storageKey(key), encrypt(value)).apply()
    }

    actual fun load(key: String): String? {
        val storageKey = storageKey(key)
        val encrypted = prefs.getString(storageKey, null) ?: return null

        return runCatching { decrypt(encrypted) }
            .getOrElse {
                prefs.edit().remove(storageKey).apply()
                null
            }
    }

    actual fun delete(key: String) {
        prefs.edit().remove(storageKey(key)).apply()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return listOf(
                FORMAT_VERSION,
                Base64.getEncoder().encodeToString(cipher.iv),
                Base64.getEncoder().encodeToString(ciphertext)
            )
            .joinToString(SEPARATOR)
    }

    private fun decrypt(encoded: String): String {
        val parts = encoded.split(SEPARATOR)
        require(parts.size == 3 && parts[0] == FORMAT_VERSION) { "Unsupported vault entry" }

        val iv = Base64.getDecoder().decode(parts[1])
        val ciphertext = Base64.getDecoder().decode(parts[2])
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun storageKey(key: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingEntry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingEntry != null) {
            return existingEntry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keySpec =
            KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .setRandomizedEncryptionRequired(true)
                .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private companion object {
        private const val PREFS_NAME = "sonic_signature_secure_prefs"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "sonic_signature_vault_aes"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE_BITS = 256
        private const val GCM_TAG_BITS = 128
        private const val FORMAT_VERSION = "v1"
        private const val SEPARATOR = ":"
    }
}
