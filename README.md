# Sonic Signature

IEM recommendation app. You give it songs you like, it analyzes audio features via Spotify and uses an LLM to recommend in-ear monitors that match your listening profile.

## Architecture

Kotlin Multiplatform. Four Gradle modules:

- **shared** -- ViewModels, API clients, recommendation engine. Common code for all platforms.
- **androidApp** -- Compose UI targeting Android.
- **desktopApp** -- Compose Desktop (JVM).
- **backend** -- Ktor server. Handles Spotify API authentication and proxies requests.

The app never talks to Spotify directly. All Spotify calls go through the backend, which manages its own Client Credentials token.

## Prerequisites

- JDK 17+
- Android SDK (API 34)
- A Spotify Developer app (Client ID + Secret)
- An LLM API key (Gemini or OpenRouter)

## Setup

1. Clone the repo.

2. Create `backend/.env`:
```
SPOTIFY_CLIENT_ID=your_client_id
SPOTIFY_CLIENT_SECRET=your_client_secret
```

3. Register `http://127.0.0.1:8080/auth/spotify/callback` as a redirect URI in your Spotify Developer Dashboard (not currently used by Client Credentials flow, but required by Spotify app config).

## Running

Start the backend first, then the app.

**Backend:**
```bash
./gradlew :backend:run
```

**Android (emulator):**
```bash
./gradlew :androidApp:installDebug
```
Default backend URL points to `10.0.2.2:8080` (emulator alias for host localhost).

**Android (physical device):**

Set your machine's local IP in `AppNavGraph.kt`:
```kotlin
BackendConfig.baseUrl = "http://<YOUR_LOCAL_IP>:8080"
```
Phone and computer must be on the same network.

**Desktop:**
```bash
./gradlew :desktopApp:run
```
Backend URL is set to `localhost:8080` automatically.

## Configuration

Open the app, go to Settings, select your LLM provider (Gemini or OpenRouter), and enter your API key. Keys are stored in platform-specific secure storage (Android Keystore / macOS Keychain / encrypted preferences on desktop).

Spotify credentials live exclusively in `backend/.env`. The app has no knowledge of them.

## Project Structure

```
sonic-signature/
  backend/
    .env                          # Spotify credentials (gitignored)
    src/main/kotlin/.../
      Application.kt             # Ktor entry point
      service/SpotifyOAuthService.kt  # Client Credentials token management
      routes/SpotifyProxyRoutes.kt    # /api/spotify/search, /audio-features
  shared/
    src/commonMain/kotlin/.../
      api/                        # SpotifyClient, LLM providers, BackendConfig
      engine/                     # RecommendationEngine, PromptBuilder, Parser
      model/                      # SongMetadata, AudioFeatures, IEMRecommendation
      viewmodel/                  # SearchViewModel, RecommendationViewModel, SettingsVM
      storage/                    # SecureVault (expect/actual), VaultKeys
  androidApp/                     # Compose UI + navigation
  desktopApp/                     # Compose Desktop window
```

## License

Not yet specified.
