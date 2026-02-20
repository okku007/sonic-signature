# Tasks: IEM Tonal Mapping Engine
**Version:** 1.0  
**Status:** Ready  
**Last Updated:** 2026-02-19  
**References:** spec.md, plan.md

---

## Legend
- `[ ]` — Not started
- `[/]` — In progress
- `[x]` — Complete
- **[SHARED]** — Lives in `shared/commonMain`
- **[ANDROID]** — Lives in `androidApp` or `androidMain`
- **[IOS]** — Lives in `iosApp` or `iosMain`
- **[DESKTOP]** — Lives in `desktopApp` or `desktopMain`

---

## Phase 0 — Project Scaffolding

- [ ] **T-001** Initialize KMP project with Gradle using `kotlin("multiplatform")` plugin
  - Targets: `androidTarget`, `iosArm64`, `iosSimulatorArm64`, `jvm` (desktop)
  - Source sets: `commonMain`, `androidMain`, `iosMain`, `desktopMain`

- [ ] **T-002** Create `libs.versions.toml` with all dependency versions from plan.md §6
  - Kotlin 2.0.21, Ktor 3.0.3, kotlinx-serialization 1.7.3, Compose MP 1.7.3, multiplatform-settings 1.2.0

- [ ] **T-003** Configure `build.gradle.kts` for each module
  - `shared/build.gradle.kts` — declare all KMP source sets and dependencies
  - `androidApp/build.gradle.kts` — Android app config (minSdk 26, Compose)
  - `desktopApp/build.gradle.kts` — JVM desktop config (JVM 17)

- [ ] **T-004** Set up Kotlin serialization Gradle plugin in root `build.gradle.kts`

- [ ] **T-005** Verify project builds cleanly on all targets (`./gradlew build`)

---

## Phase 1 — Shared Data Models [SHARED]

- [ ] **T-010** Create `model/BudgetTier.kt`
  ```kotlin
  enum class BudgetTier(val label: String, val range: String) {
      ULTRA_BUDGET("Ultra-Budget", "<$25"),
      ENTRY("Entry", "$25–$80"),
      MID_RANGE("Mid-Range", "$100–$500"),
      HIGH_END("High-End", ">$1,000")
  }
  ```

- [ ] **T-011** Create `model/SongMetadata.kt`
  - Fields: `id`, `name`, `artist`, `albumArt`, `genres: List<String>`

- [ ] **T-012** Create `model/AudioFeatures.kt`
  - Fields: `id`, `tempo`, `energy`, `valence`, `acousticness`, `speechiness`, `danceability`, `instrumentalness`
  - Annotate with `@Serializable`

- [ ] **T-013** Create `model/IEMRecommendation.kt`
  - Fields: `name`, `brand`, `priceUSD`, `driverType: DriverType`, `soundSignature: SoundSignature`, `crinacleGrade: String?`, `justification`
  - Create `enum class DriverType { DD, BA, PLANAR, HYBRID, TRIBRID }`
  - Create `enum class SoundSignature { NEUTRAL, V_SHAPED, WARM, BRIGHT, BALANCED }`

- [ ] **T-014** Create `util/Result.kt`
  ```kotlin
  sealed class Result<out T> {
      data class Success<T>(val data: T) : Result<T>()
      data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
      object Loading : Result<Nothing>()
  }
  ```

---

## Phase 2 — Secure Storage [SHARED + PLATFORM]

- [ ] **T-020** Create `storage/SecureVault.kt` in `commonMain` — `expect class` with `save()`, `load()`, `delete()`

- [ ] **T-021** Implement `storage/SecureVault.android.kt` in `androidMain`
  - Use `EncryptedSharedPreferences` with `AES256_SIV` key scheme

- [ ] **T-022** Implement `storage/SecureVault.ios.kt` in `iosMain`
  - Use Keychain Services via `Security` framework interop

- [ ] **T-023** Implement `storage/SecureVault.desktop.kt` in `desktopMain`
  - Use `multiplatform-settings` with JVM preferences as fallback
  - Note: Full OS keychain integration (libsecret/DPAPI) deferred to v1.1

- [ ] **T-024** Define storage key constants in `storage/VaultKeys.kt`
  ```kotlin
  object VaultKeys {
      const val SPOTIFY_CLIENT_ID = "spotify_client_id"
      const val SPOTIFY_CLIENT_SECRET = "spotify_client_secret"
      const val LLM_PROVIDER = "llm_provider"
      const val LLM_API_KEY = "llm_api_key"
      const val OPENROUTER_MODEL_ID = "openrouter_model_id"
  }
  ```

---

## Phase 3 — Spotify API Client [SHARED]

- [ ] **T-030** Create `api/SpotifyAuthManager.kt`
  - Implement Client Credentials flow: POST to `https://accounts.spotify.com/api/token`
  - Cache access token with expiry; auto-refresh when expired
  - Read client ID/secret from `SecureVault`

- [ ] **T-031** Create `api/SpotifyClient.kt`
  - Configure Ktor `HttpClient` with `ContentNegotiation` (JSON) and `Auth` plugins
  - Implement `searchTracks(query: String, limit: Int = 10): Result<List<SongMetadata>>`
    - `GET /v1/search?q={query}&type=track&limit={limit}`
  - Implement `getAudioFeatures(trackId: String): Result<AudioFeatures>`
    - `GET /v1/audio-features/{id}`

- [ ] **T-032** Write unit tests for `SpotifyClient` using Ktor `MockEngine`
  - Test: successful search returns parsed `SongMetadata` list
  - Test: 401 response triggers token refresh
  - Test: network error returns `Result.Error`

---

## Phase 4 — LLM Client [SHARED]

- [ ] **T-040** Create `api/LLMProvider.kt` — interface with `suspend fun complete(prompt: String): String`

- [ ] **T-041** Create `api/GeminiProvider.kt` implementing `LLMProvider`
  - POST to `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent`
  - Use `apiKey` query param
  - Parse `candidates[0].content.parts[0].text`

- [ ] **T-042** Create `api/OpenRouterProvider.kt` implementing `LLMProvider`
  - Base URL: `https://openrouter.ai/api/v1`
  - POST to `/chat/completions` with OpenAI-compatible request body
  - Include `HTTP-Referer` and `X-Title` headers per OpenRouter requirements
  - Read `modelId` from `SecureVault`

- [ ] **T-043** Create `api/LLMClientFactory.kt`
  - Reads `LLM_PROVIDER` from `SecureVault` and returns the correct `LLMProvider` implementation

- [ ] **T-044** Write unit tests for LLM providers using Ktor `MockEngine`
  - Test: Gemini provider parses response correctly
  - Test: OpenRouter provider constructs correct request body
  - Test: API error (4xx) returns descriptive error message

---

## Phase 5 — Recommendation Engine [SHARED]

- [ ] **T-050** Create `engine/PromptBuilder.kt`
  - `fun buildSongPrompt(song: SongMetadata, features: AudioFeatures, budget: BudgetTier): String`
  - `fun buildGenrePrompt(genreDescription: String, budget: BudgetTier): String`
  - Use prompt templates from plan.md §4

- [ ] **T-051** Create `engine/RecommendationParser.kt`
  - `fun parse(llmResponse: String): Result<List<IEMRecommendation>>`
  - Extract JSON array from LLM response (handle markdown code fences)
  - Deserialize using `kotlinx.serialization`
  - Return `Result.Error` with user-friendly message on parse failure

- [ ] **T-052** Create `engine/RecommendationEngine.kt`
  - `suspend fun recommendFromSong(trackId: String, budget: BudgetTier): Result<List<IEMRecommendation>>`
    1. Fetch audio features via `SpotifyClient`
    2. Build prompt via `PromptBuilder`
    3. Call `LLMProvider.complete()`
    4. Parse response via `RecommendationParser`
  - `suspend fun recommendFromGenre(description: String, budget: BudgetTier): Result<List<IEMRecommendation>>`

- [ ] **T-053** Write unit tests for `RecommendationEngine`
  - Test: full happy-path flow with mocked Spotify + LLM responses
  - Test: Spotify failure short-circuits and returns `Result.Error`
  - Test: LLM parse failure returns `Result.Error` with correct message

---

## Phase 6 — ViewModels [SHARED]

- [ ] **T-060** Create `viewmodel/SearchViewModel.kt`
  - State: `searchQuery`, `searchResults: List<SongMetadata>`, `isLoading`, `error`
  - `fun onQueryChanged(query: String)` — debounced (300ms) call to `SpotifyClient.searchTracks()`

- [ ] **T-061** Create `viewmodel/RecommendationViewModel.kt`
  - State: `selectedTrack`, `selectedBudget`, `recommendations`, `isLoading`, `error`
  - `fun onTrackSelected(track: SongMetadata)`
  - `fun onBudgetSelected(tier: BudgetTier)`
  - `fun getRecommendations()` — calls `RecommendationEngine`

- [ ] **T-062** Create `viewmodel/SettingsViewModel.kt`
  - State: `llmProvider`, `apiKey` (masked), `openRouterModelId`, `spotifyClientId`
  - `fun saveSettings(...)` — writes to `SecureVault`
  - `fun validateApiKey()` — makes a minimal test call to the LLM

---

## Phase 7 — Android UI [ANDROID]

- [ ] **T-070** Set up Jetpack Compose with Material 3 theme in `androidApp`

- [ ] **T-071** Implement `HomeScreen.kt`
  - Tab bar: "Song Search" | "Genre/Mood"
  - Search bar with autocomplete dropdown (driven by `SearchViewModel`)
  - Budget tier selector (chip group)
  - "Find My IEM" CTA button

- [ ] **T-072** Implement `ResultsScreen.kt`
  - Lazy column of `IEMRecommendationCard` composables
  - Each card: IEM name, brand, price, driver type badge, sound signature badge, short justification
  - Tappable → navigates to `DetailScreen`

- [ ] **T-073** Implement `DetailScreen.kt`
  - Full recommendation detail: all fields + full justification
  - Crinacle grade badge (color-coded: A=green, B=blue, C=yellow)
  - "View on Crinacle" link (opens browser)

- [ ] **T-074** Implement `SettingsScreen.kt`
  - LLM provider picker (Gemini / OpenRouter)
  - API key input (masked, with show/hide toggle)
  - OpenRouter model ID input (shown only when OpenRouter selected)
  - Spotify credentials section
  - "Validate & Save" button

- [ ] **T-075** Set up Navigation (Compose Navigation) with routes: `home`, `results`, `detail/{iemIndex}`, `settings`

---

## Phase 8 — Desktop UI [DESKTOP]

- [ ] **T-080** Set up Compose Multiplatform desktop app in `desktopApp`

- [ ] **T-081** Implement desktop `MainWindow.kt` with two-panel layout
  - Left panel: search + budget selector
  - Right panel: results list

- [ ] **T-082** Reuse shared composables from Android UI where possible (extract to `shared/commonMain/ui/`)

- [ ] **T-083** Implement desktop `SettingsDialog.kt` (modal dialog)

---

## Phase 9 — iOS UI [IOS]

- [ ] **T-090** Set up iOS app in `iosApp` with Compose Multiplatform or SwiftUI bridge

- [ ] **T-091** Implement iOS `HomeView` mirroring Android `HomeScreen` functionality

- [ ] **T-092** Implement iOS `ResultsView` and `DetailView`

- [ ] **T-093** Implement iOS `SettingsView`

---

## Phase 10 — Integration & Polish

- [ ] **T-100** Add loading states and skeleton screens to all result views

- [ ] **T-101** Implement error snackbar/toast system tied to `Result.Error` states

- [ ] **T-102** Add input validation: prevent "Find My IEM" if no API key configured (show onboarding prompt)

- [ ] **T-103** Implement 300ms debounce on search input to reduce Spotify API calls

- [ ] **T-104** Add "Clear results" and "Search again" actions on results screen

- [ ] **T-105** Accessibility: ensure all interactive elements have content descriptions / semantic labels

---

## Phase 11 — Testing & Verification

- [ ] **T-110** Run all unit tests: `./gradlew :shared:test`

- [ ] **T-111** Run Android instrumented tests: `./gradlew :androidApp:connectedAndroidTest`

- [ ] **T-112** Manual smoke test — Android:
  1. Launch app → verify Settings screen appears (no API key configured)
  2. Enter Gemini API key → tap "Validate & Save" → verify success toast
  3. Return to Home → search "Bohemian Rhapsody" → verify autocomplete shows results
  4. Select track → select "Mid-Range" budget → tap "Find My IEM"
  5. Verify 3 recommendations appear with name, price, driver type, justification
  6. Tap a recommendation → verify detail screen shows all fields

- [ ] **T-113** Manual smoke test — Desktop: repeat T-112 steps on desktop app

- [ ] **T-114** Security audit: confirm no API keys appear in logcat / console logs

---

## Deferred to v2.0

- [ ] **T-200** Koog agentic workflow for playlist-level analysis
- [ ] **T-201** MCP integration for real-time IEM price lookups
- [ ] **T-202** Full OS keychain integration for Desktop (libsecret / DPAPI)
- [ ] **T-203** Crinacle live grade data integration
