package com.sonicsignature.storage

/**
 * Platform-agnostic secure storage interface.
 * Each platform provides its own `actual` implementation using native secure storage.
 * Constitution §1.2: Keys must never be logged or stored in plaintext.
 */
expect class SecureVault() {
    fun save(key: String, value: String)
    fun load(key: String): String?
    fun delete(key: String)
}
