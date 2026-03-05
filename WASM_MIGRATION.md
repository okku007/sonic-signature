# Web App (WasmJs) Integration Guide

This document outlines the architectural changes and additions made to integrating the Web App target into the `sonic-signature` Kotlin Multiplatform project.

## 1. Shared UI Architecture (`:shared`)
The existing Jetpack Compose UI was migrated from the `androidApp` module into the `shared/src/commonMain/` source set to permit cross-platform consumption.

- **Compose Multiplatform Configuration:** The `shared` module now applies the `org.jetbrains.compose` and `org.jetbrains.kotlin.plugin.compose` plugins.
- **Wasm Target:** Configured `wasmJs { browser { ... } binaries.executable() }` within `shared/build.gradle.kts`.
- **Dependencies:** Compose UI rendering across the Web requires standard libraries (`compose.runtime`, `compose.foundation`, `compose.material3`, `compose.components.resources`) included securely within `commonMain`.

### Refactored Platform Dependencies
To successfully migrate UI components natively across Android, Desktop, and the Web browser:
- **Intents/External Links:** `android.content.Intent` and `android.net.Uri` (which were breaking Wasm/Desktop compilation) were uniformly replaced with Compose Multiplatform's `LocalUriHandler.current.openUri(...)`.
- **Theming:** Colors originally sourced directly from the Android module were abstracted natively to Compose's `androidx.compose.ui.graphics.Color` to decouple them.

## 2. Platform-Specific Implementations

### Secure Storage Enclave (`SecureVault.kt`)
Because Web browsers (Wasm) lack standard keystores/DPAPI access without relying on asynchronous WebCrypto setups, the previously common `SecureVault` class was refactored into an `expect` / `actual` hierarchy:
1. **Desktop (`desktopMain`):** Continues using JVM `java.util.prefs.Preferences` with AES-256-GCM encryption.
2. **WasmJs (`wasmJsMain`):** Utilizes `multiplatform-settings` mapped to standard browser `localStorage`. Rather than full encryption, it employs base-level XOR Base64 obfuscation to deter casual scraping, as the Web App runs entirely locally without a backend.

## 3. Web Target (`:webApp`)
A new lightweight Gradle module was constructed as the distribution point for the web application.

- **Build Configuration (`webApp/build.gradle.kts`):** Extends `:shared`, applying Ktor's content negotiation libraries and Compose Web runtime settings natively. 
- **DOM Entry Point (`index.html`):** The primary host page rendering a generic HTML `<canvas id="compose-target">` while executing the compiled `.js` artifacts.
- **Application Startup (`Main.kt`):** Bootstraps `CanvasBasedWindow` into the DOM. Handles instantiation of core dependencies (like `HttpClient` and `SecureVault`) and sets up a bespoke memory-based router tailored for Web navigation.

## 4. Workarounds and Caveats
- **Gradle Node.js Resolution:** To allow Kotlin Multiplatform plugins to accurately download Node.js for Wasm packaging via Webpack, the project's root `settings.gradle.kts` was adjusted to use `RepositoriesMode.PREFER_PROJECT` vs `FAIL_ON_PROJECT_REPOS`.

## Running the Web App Locally
To test the web app distribution in development mode with continuous compilation (Hot Reload), invoke:
```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```
The application will be served at `http://localhost:8080/`.
