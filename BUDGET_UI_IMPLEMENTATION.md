# Budget Input UI Setup and Troubleshooting

## Overview
This document outlines the implementation of the "Exact Budget Input" feature completed for the Sonic Signature application. It details the architectural changes made, the files affected, and the specific troubleshooting steps taken to resolve cross-platform compilation issues and local environment constraints.

## Changes Implemented

### 1. Model and State Refactoring
- **Goal:** Allow users to input exact integer budget amounts (e.g., ₹25,000) instead of limiting them to broad, predefined `BudgetTier` enums (like "ENTRY" or "MID_RANGE").
- **Changes:**
  - Modified `RecommendationViewModel`'s `selectedBudget` state type from `BudgetTier` to `Int` (defaulting to 40,000).
  - Updated `RecommendationEngine` and `PromptBuilder` to accept and process the `Int` budget directly. `PromptBuilder` now actively injects the exact integer into the LLM system prompt for precise results.

### 2. UI Updates (`DiscoverScreen` & `BudgetSlider`)
- **Goal:** Provide a flexible UI that supports both a slider and direct text input for price constraints.
- **Changes:**
  - Overhauled `BudgetSlider.kt` to include a `BasicTextField` hooked to numeric-only inputs.
  - The `Slider` component was modified to use discrete steps (increments of ₹1,000) for better usability.
  - `DiscoverScreen.kt` was updated to seamlessly synchronize the state between the text field, the slider, and the `RecommendationViewModel`.

## Issues Faced & Resolutions

### 1. Cross-Platform Signature Mismatches
- **Problem:** When changing the `onBudgetSelected` callback in `DiscoverScreen` from passing a `BudgetTier` object to an `Int`, the compilation broke across other Kotlin Multiplatform targets (Android, Desktop, WebApp). We also encountered missing callback arguments (`onTuningSelected`, `onCustomTuningPreferenceChanged`).
- **Solution:** Iteratively updated the routing/main classes across all platform sources:
  - **Android:** `AppNavGraph.kt`
  - **Desktop:** `MainWindow.kt`
  - **Web:** `Main.kt`
  We ensured all invocations of `DiscoverScreen` passed the required `Int` and tuning callbacks.

### 2. Unresolved Reference Errors (Ktor & Serialization)
- **Problem:** During desktop UI compilation (`desktopApp/src/desktopMain/kotlin/com/sonicsignature/desktop/ui/MainWindow.kt`), we encountered "Unresolved reference" errors for `Json`, `ignoreUnknownKeys`, and `HttpClient`.
- **Cause:** These libraries were included in the `shared` module via the `implementation` configuration in Gradle, which hides transitive dependencies from the consuming platform apps (Desktop/WebApp).
- **Solution:** Updated `shared/build.gradle.kts` to expose `ktor.client.core`, `ktor.client.content.negotiation`, `ktor.serialization.kotlinx.json`, and `kotlinx.serialization.json` using the `api` configuration instead of `implementation`. This propagates the dependencies to targets that depend on the `shared` module, resolving the missing imports.

### 3. Local RAM Constraints & Wasm Compilation
- **Problem:** Running `./gradlew assemble -x test` on the local machine frequently caused the system/IDE to hang or crash due to out-of-memory (OOM) errors. 
- **Cause:** The `wasmJs` compilation tasks (`compileProductionExecutableKotlinWasmJs` and `wasmJsNpmAggregated`) are highly resource-intensive. When run in parallel with Android, iOS, and Desktop builds, it easily overwhelmed the laptop's available RAM.
- **Solution/Takeaway:** 
  - Be cautious when running full multi-platform `assemble` commands on resource-constrained development machines.
  - Isolate build verifications. For example, testing the Android variant via `./gradlew :androidApp:installDebug` is significantly faster and less resource-hungry than compiling the entire cross-platform matrix at once.
