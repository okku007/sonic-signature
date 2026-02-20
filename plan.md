# Plan: IEM Tonal Mapping Engine — Technical Architecture
**Version:** 1.0  
**Status:** Draft  
**Last Updated:** 2026-02-19  
**References:** spec.md

---

## 1. Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    KMP Shared Module (commonMain)           │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ SpotifyClient│  │  LLMClient   │  │  SecureVault     │  │
│  │  (Ktor)      │  │  (Ktor/BYOM) │  │  (expect/actual) │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                 │                    │            │
│  ┌──────▼─────────────────▼────────────────────▼─────────┐  │
│  │              RecommendationEngine (ViewModel)          │  │
│  │   - fetchAudioFeatures()                               │  │
│  │   - buildPrompt()                                      │  │
│  │   - parseRecommendations()                             │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
         │                    │                    │
   ┌─────▼──────┐      ┌──────▼──────┐     ┌──────▼──────┐
   │  Android   │      │    iOS      │     │   Desktop   │
   │  (Compose) │      │  (SwiftUI/  │     │  (Compose   │
   │            │      │  Compose MP)│     │   MP)       │
   └────────────┘      └─────────────┘     └─────────────┘
```

---

## 2. Module Structure

```
sonic-signature/
├── shared/
│   └── src/
│       ├── commonMain/kotlin/
│       │   ├── api/
│       │   │   ├── SpotifyClient.kt        # Ktor-based Spotify API wrapper
│       │   │   └── LLMClient.kt            # Unified BYOM LLM client (Gemini + OpenRouter)
│       │   ├── model/
│       │   │   ├── SongMetadata.kt         # @Serializable data classes
│       │   │   ├── AudioFeatures.kt
│       │   │   ├── IEMRecommendation.kt
│       │   │   └── BudgetTier.kt           # Enum: ULTRA_BUDGET, ENTRY, MID_RANGE, HIGH_END
│       │   ├── engine/
│       │   │   ├── RecommendationEngine.kt # Core orchestration logic
│       │   │   └── PromptBuilder.kt        # Dynamic prompt construction
│       │   ├── storage/
│       │   │   └── SecureVault.kt          # expect class — platform-delegated
│       │   └── util/
│       │       └── Result.kt               # Sealed class: Success / Error / Loading
│       ├── androidMain/kotlin/
│       │   └── storage/SecureVault.android.kt   # EncryptedSharedPreferences
│       ├── iosMain/kotlin/
│       │   └── storage/SecureVault.ios.kt       # Keychain Services
│       └── desktopMain/kotlin/
│           └── storage/SecureVault.desktop.kt   # libsecret / Credential Locker
├── androidApp/
├── iosApp/
└── desktopApp/
```

---

## 3. Data Contracts

### 3.1 Spotify API

**Search Request**
```
GET https://api.spotify.com/v1/search
  ?q={query}
  &type=track
  &limit=10
Authorization: Bearer {access_token}
```

**Audio Features Response → AudioFeatures model**
```kotlin
@Serializable
data class AudioFeatures(
    val id: String,
    val tempo: Double,          // BPM
    val energy: Double,         // 0.0–1.0
    val valence: Double,        // 0.0–1.0 (mood positiveness)
    val acousticness: Double,   // 0.0–1.0
    val speechiness: Double,    // 0.0–1.0
    val danceability: Double,   // 0.0–1.0 (bonus signal)
    val instrumentalness: Double
)
```

**Auth Flow:** Client Credentials (server-side token fetch) for search; PKCE for user-specific data.  
> ⚠️ Spotify Client ID/Secret must be stored in SecureVault, never in source code.

### 3.2 LLM Client

**Provider abstraction:**
```kotlin
interface LLMProvider {
    suspend fun complete(prompt: String): String
}

class GeminiProvider(apiKey: String) : LLMProvider
class OpenRouterProvider(apiKey: String, modelId: String) : LLMProvider
```

**OpenRouter base URL:** `https://openrouter.ai/api/v1`  
**Gemini REST endpoint:** `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`

### 3.3 Recommendation Output Model
```kotlin
@Serializable
data class IEMRecommendation(
    val name: String,
    val brand: String,
    val priceUSD: Int,
    val driverType: DriverType,         // DD, BA, PLANAR, HYBRID, TRIBRID
    val soundSignature: SoundSignature, // NEUTRAL, V_SHAPED, WARM, BRIGHT, BALANCED
    val crinacleGrade: String?,         // e.g. "A", "B+", null if unknown
    val justification: String
)
```

---

## 4. Prompt Engineering

### 4.1 Song-Based Prompt Template
```
You are an expert audiophile consultant specializing in In-Ear Monitors (IEMs).
Analyze the following music metadata and recommend exactly 3 IEMs.

SONG METADATA:
- Track: {name} by {artist}
- Genre(s): {genres}
- BPM: {tempo}
- Energy: {energy} (0=calm, 1=intense)
- Valence: {valence} (0=dark/sad, 1=happy/euphoric)
- Acousticness: {acousticness} (0=synthetic, 1=organic/acoustic)
- Speechiness: {speechiness} (0=instrumental, 1=spoken word)

BUDGET: {budgetTier} ({budgetRange})

INSTRUCTIONS:
- Ground recommendations in Crinacle's IEM ranking database
- For each IEM, provide: name, brand, approximate USD price, driver type, sound signature, Crinacle grade (if known), and a 2-sentence technical justification
- Respond ONLY with a valid JSON array matching this schema:
  [{"name":"...","brand":"...","priceUSD":0,"driverType":"DD|BA|PLANAR|HYBRID|TRIBRID","soundSignature":"NEUTRAL|V_SHAPED|WARM|BRIGHT|BALANCED","crinacleGrade":"A|B+|...|null","justification":"..."}]
```

### 4.2 Genre-Based Prompt Template
```
You are an expert audiophile consultant specializing in In-Ear Monitors (IEMs).
The user listens primarily to: {genreOrMoodDescription}

BUDGET: {budgetTier} ({budgetRange})

INSTRUCTIONS: (same as above)
```

---

## 5. Security Architecture

| Platform | Mechanism | Library |
|---|---|---|
| Android | EncryptedSharedPreferences / Android KeyStore | `androidx.security:security-crypto` |
| iOS | Keychain Services (Secure Enclave) | Native via `actual` |
| macOS | System Keychain | `KeychainSettings` |
| Windows | Windows Credential Locker | `DPAPI` via JNA |
| Linux | libsecret / GNOME Keyring | `libsecret` via JNA |

**Shared interface (commonMain):**
```kotlin
expect class SecureVault() {
    fun save(key: String, value: String)
    fun load(key: String): String?
    fun delete(key: String)
}
```

**Keys stored:**
- `spotify_client_id`
- `spotify_client_secret`
- `llm_provider` (enum string)
- `llm_api_key`
- `openrouter_model_id`

---

## 6. Dependency Versions (libs.versions.toml)

```toml
[versions]
kotlin = "2.0.21"
ktor = "3.0.3"
kotlinx-serialization = "1.7.3"
kotlinx-coroutines = "1.9.0"
compose-multiplatform = "1.7.3"
androidx-security-crypto = "1.1.0-alpha06"
multiplatform-settings = "1.2.0"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
ktor-serialization-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
multiplatform-settings = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatform-settings" }
```

---

## 7. Error Handling Strategy

All async operations return `Result<T>`:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

**Error scenarios and UI responses:**

| Scenario | User-Facing Message |
|---|---|
| No API key configured | "Set up your LLM provider in Settings to get started." |
| Spotify auth failure | "Couldn't connect to Spotify. Check your credentials in Settings." |
| LLM API error (4xx) | "Your API key may be invalid. Please check Settings." |
| LLM API error (5xx / timeout) | "The AI service is temporarily unavailable. Try again." |
| No Spotify results | "No tracks found. Try a different search." |
| LLM returned invalid JSON | "Couldn't parse recommendations. Try again or switch models." |

---

## 8. Platform-Specific Notes

### Android
- Min SDK: 26 (Android 8.0) — required for `EncryptedSharedPreferences`
- Ktor engine: `OkHttp`
- UI: Jetpack Compose

### iOS
- Min deployment target: iOS 16
- Ktor engine: `Darwin`
- UI: Compose Multiplatform (or SwiftUI bridge)

### Desktop
- JVM target: 17+
- Ktor engine: `CIO`
- UI: Compose Multiplatform

---

## 9. Future Considerations (v2.0)

- **Koog agentic workflows** for playlist-level analysis (multi-song holistic signature)
- **MCP (Model Context Protocol)** integration for real-time IEM price lookups
- **Crinacle API** or scraper for live grade data
- **Offline mode** with bundled IEM database snapshot
