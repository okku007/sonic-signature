package com.sonicsignature.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop actual implementation of SecureVault with AES-256-GCM encryption.
 *
 * All values are encrypted before being stored in JVM Preferences. The encryption key is a random
 * AES-256 key generated on first launch and saved to `~/.sonic-signature/vault.key` with owner-only
 * permissions (600).
 *
 * This ensures that even if someone reads the JVM Preferences XML file, they get encrypted
 * gibberish. The encryption key is file-permission protected separately.
 *
 * Note: Full OS keychain integration (libsecret/DPAPI via JNA) is deferred to v1.1.
 */
actual class SecureVault {

    private val settings: Settings = Settings()
    private val secretKey: SecretKey = loadOrCreateKey()

    companion object {
        private const val KEY_SIZE_BYTES = 32 // 256 bits
        private const val GCM_IV_SIZE_BYTES = 12
        private const val GCM_TAG_BITS = 128
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private val keyDir: File = File(System.getProperty("user.home"), ".sonic-signature")
        private val keyFile: File = File(keyDir, "vault.key")
    }

    actual fun save(key: String, value: String) {
        val encrypted = encrypt(value)
        settings[key] = encrypted
    }

    actual fun load(key: String): String? {
        val encrypted = settings.getStringOrNull(key) ?: return null
        return try {
            decrypt(encrypted)
        } catch (_: Exception) {
            // If decryption fails (e.g. key changed or legacy plaintext value),
            // remove the corrupted entry and return null
            settings.remove(key)
            null
        }
    }

    actual fun delete(key: String) {
        settings.remove(key)
    }

    // ── Encryption helpers ────────────────────────────────────────────────────

    private fun encrypt(plaintext: String): String {
        val iv = ByteArray(GCM_IV_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        // Prepend IV to ciphertext: [IV (12 bytes) | ciphertext+tag]
        val combined = iv + ciphertext
        return Base64.getEncoder().encodeToString(combined)
    }

    private fun decrypt(encoded: String): String {
        val combined = Base64.getDecoder().decode(encoded)
        val iv = combined.copyOfRange(0, GCM_IV_SIZE_BYTES)
        val ciphertext = combined.copyOfRange(GCM_IV_SIZE_BYTES, combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    // ── Key management ────────────────────────────────────────────────────────

    private fun loadOrCreateKey(): SecretKey {
        if (keyFile.exists()) {
            val keyBytes = Base64.getDecoder().decode(keyFile.readText().trim())
            return SecretKeySpec(keyBytes, ALGORITHM)
        }

        // Generate a new random AES-256 key
        val keyBytes = ByteArray(KEY_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        keyDir.mkdirs()
        keyFile.writeText(Base64.getEncoder().encodeToString(keyBytes))

        // Set owner-only permissions (600) on Unix systems
        try {
            Files.setPosixFilePermissions(
                    keyFile.toPath(),
                    PosixFilePermissions.fromString("rw-------")
            )
        } catch (_: UnsupportedOperationException) {
            // Windows — skip POSIX permissions, rely on user-scoped directory
        }

        return SecretKeySpec(keyBytes, ALGORITHM)
    }
}
