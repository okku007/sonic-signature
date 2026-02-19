package com.sonicsignature.storage

import android.content.Context

/**
 * Holds the Android application context for use in platform-specific implementations.
 * Must be initialized in Application.onCreate() before any SecureVault usage.
 */
object ApplicationContextHolder {
    lateinit var context: Context
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}
