package com.sonicsignature.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.CanvasBasedWindow
import com.sonicsignature.api.LLMClientFactory
import com.sonicsignature.api.MusicClient
import com.sonicsignature.engine.RecommendationEngine
import com.sonicsignature.storage.SecureVault
import com.sonicsignature.storage.VaultKeys
import com.sonicsignature.ui.screens.*
import com.sonicsignature.viewmodel.RecommendationViewModel
import com.sonicsignature.viewmodel.SearchViewModel
import com.sonicsignature.viewmodel.SettingsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "compose-target", title = "Sonic Signature") {
        WebAppRoot()
    }
}

// Simple Web routing enum for KMP UI without navigating-compose directly
private enum class WebRoute {
    HOME,
    RESULTS,
    DETAIL,
    SETTINGS
}

@Composable
fun WebAppRoot() {
    // ── Dependency wiring (similar to Android AppNavGraph) ──────────────
    val vault = remember { SecureVault() }
    val httpClient = remember {
        HttpClient {
            install(ContentNegotiation) {
                json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        }
                )
            }
        }
    }

    val settingsVM = remember { SettingsViewModel(vault, httpClient) }
    val settingsState by settingsVM.state.collectAsState()

    val hasLastFmKey = settingsState.lastFmKeyMasked.isNotEmpty()
    val lastFmKey =
            remember(hasLastFmKey) {
                if (hasLastFmKey) vault.load(VaultKeys.LASTFM_API_KEY) ?: "" else ""
            }

    val musicClient = remember(lastFmKey) { MusicClient(httpClient, lastFmKey) }
    val llmFactory = remember { LLMClientFactory(httpClient, vault) }
    val engine = remember(lastFmKey) { RecommendationEngine(musicClient, llmFactory) }

    val searchVM = remember(lastFmKey) { SearchViewModel(musicClient) }
    val recVM = remember(lastFmKey) { RecommendationViewModel(engine) }

    val searchState by searchVM.state.collectAsState()
    val recState by recVM.state.collectAsState()

    val hasApiKey = settingsState.apiKeyMasked.isNotEmpty()

    // Base navigation
    var currentRoute by remember { mutableStateOf(WebRoute.HOME) }
    var selectedDetailIndex by remember { mutableStateOf<Int?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            when (currentRoute) {
                WebRoute.HOME -> {
                    DiscoverScreen(
                            searchState = searchState,
                            recommendationState = recState,
                            onQueryChanged = searchVM::onQueryChanged,
                            onTrackSelected = { track ->
                                recVM.onTrackSelected(track)
                                searchVM.clearResults()
                            },
                            onTrackRemoved = recVM::onTrackRemoved,
                            onInputModeChanged = recVM::onInputModeChanged,
                            onArtistAdded = recVM::onArtistAdded,
                            onArtistRemoved = recVM::onArtistRemoved,
                            onGenreDescriptionChanged = recVM::onGenreDescriptionChanged,
                            onBudgetSelected = recVM::onBudgetSelected,
                            onTuningSelected = recVM::onTuningSelected,
                            onCustomTuningPreferenceChanged =
                                    recVM::onCustomTuningPreferenceChanged,
                            onFindMyIEM = {
                                recVM.getRecommendations()
                                currentRoute = WebRoute.RESULTS
                            },
                            onNavigateToSettings = { currentRoute = WebRoute.SETTINGS },
                            hasApiKey = hasApiKey
                    )
                }
                WebRoute.RESULTS -> {
                    ResultsScreen(
                            recommendations = recState.recommendations,
                            isLoading = recState.isLoading,
                            error = recState.error,
                            onBack = { currentRoute = WebRoute.HOME },
                            onSearchAgain = {
                                recVM.clearResults()
                                currentRoute = WebRoute.HOME
                            },
                            onRecommendationClick = { index ->
                                selectedDetailIndex = index
                                currentRoute = WebRoute.DETAIL
                            }
                    )
                }
                WebRoute.DETAIL -> {
                    val idx = selectedDetailIndex ?: 0
                    val recommendation = recState.recommendations.getOrNull(idx)
                    if (recommendation != null) {
                        DetailScreen(
                                recommendation = recommendation,
                                onBack = { currentRoute = WebRoute.RESULTS }
                        )
                    } else {
                        currentRoute = WebRoute.RESULTS
                    }
                }
                WebRoute.SETTINGS -> {
                    SettingsScreen(
                            state = settingsState,
                            onBack = { currentRoute = WebRoute.HOME },
                            onSave = { provider, key, modelId ->
                                settingsVM.saveSettings(provider, key, modelId)
                            },
                            onValidate = { provider, key, modelId ->
                                settingsVM.validateApiKey(provider, key, modelId)
                            },
                            onValidateLastFmKey = settingsVM::validateLastFmKey,
                            onClearAll = settingsVM::clearAllData
                    )
                }
            }
        }
    }
}
