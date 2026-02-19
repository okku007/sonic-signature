package com.sonicsignature.api

/**
 * Configuration for the project backend server.
 * - Android emulator: 10.0.2.2 maps to host machine's localhost
 * - Physical device: set to your machine's local IP (e.g. 192.168.x.x:8080)
 * - Desktop: override to http://localhost:8080 at startup
 * - Production: override to your deployed server URL
 */
object BackendConfig {
    var baseUrl: String = "http://10.0.2.2:8080"
}
