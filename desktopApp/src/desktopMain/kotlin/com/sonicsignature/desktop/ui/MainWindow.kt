package com.sonicsignature.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.sonicsignature.android.ui.screens.*
import com.sonicsignature.api.*
import com.sonicsignature.engine.RecommendationEngine
import com.sonicsignature.storage.SecureVault
import com.sonicsignature.viewmodel.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Desktop main window — two-panel layout. Left panel: search + budget + CTA. Right panel: results.
 * Reuses shared ViewModels and screen composables from the Android module.
 */
@Composable
fun MainWindow() {
    // ── DI wiring ────────────────────────────────────────────────────────────
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
    val spotifyClient = remember { SpotifyClient(httpClient) }
    val llmFactory = remember { LLMClientFactory(httpClient, vault) }
    val engine = remember { RecommendationEngine(spotifyClient, llmFactory) }

    // Desktop uses localhost directly
    LaunchedEffect(Unit) { BackendConfig.baseUrl = "http://localhost:8080" }

    val searchVM = remember { SearchViewModel(spotifyClient) }
    val recVM = remember { RecommendationViewModel(engine) }
    val settingsVM = remember { SettingsViewModel(vault, httpClient) }

    val searchState by searchVM.state.collectAsState()
    val recState by recVM.state.collectAsState()
    val settingsState by settingsVM.state.collectAsState()

    val hasApiKey = settingsState.apiKeyMasked.isNotEmpty()

    var showSettings by remember { mutableStateOf(false) }
    var selectedRecommendationIndex by remember { mutableStateOf<Int?>(null) }

    MaterialTheme {
        if (showSettings) {
            SettingsScreen(
                    state = settingsState,
                    onBack = { showSettings = false },
                    onSave = { provider, key, modelId ->
                        settingsVM.saveSettings(provider, key, modelId)
                    },
                    onValidate = { provider, key, modelId ->
                        settingsVM.validateApiKey(provider, key, modelId)
                    },
                    onClearAll = settingsVM::clearAllData
            )
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // ── Left Panel (40%) ─────────────────────────────────────────
                Box(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
                    HomeScreen(
                            searchState = searchState,
                            recommendationState = recState,
                            onQueryChanged = searchVM::onQueryChanged,
                            onTrackSelected = { track ->
                                recVM.onTrackSelected(track)
                                searchVM.clearResults()
                            },
                            onInputModeChanged = recVM::onInputModeChanged,
                            onArtistAdded = recVM::onArtistAdded,
                            onArtistRemoved = recVM::onArtistRemoved,
                            onGenreDescriptionChanged = recVM::onGenreDescriptionChanged,
                            onBudgetSelected = recVM::onBudgetSelected,
                            onFindMyIEM = {
                                recVM.getRecommendations()
                                selectedRecommendationIndex = null
                            },
                            onNavigateToSettings = { showSettings = true },
                            hasApiKey = hasApiKey
                    )
                }

                VerticalDivider()

                // ── Right Panel (60%) ────────────────────────────────────────
                Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                    val selectedIndex = selectedRecommendationIndex
                    if (selectedIndex != null && recState.recommendations.isNotEmpty()) {
                        val rec = recState.recommendations.getOrNull(selectedIndex)
                        if (rec != null) {
                            DetailScreen(
                                    recommendation = rec,
                                    onBack = { selectedRecommendationIndex = null }
                            )
                        }
                    } else {
                        ResultsScreen(
                                recommendations = recState.recommendations,
                                isLoading = recState.isLoading,
                                error = recState.error,
                                onBack = { recVM.clearResults() },
                                onSearchAgain = { recVM.clearResults() },
                                onRecommendationClick = { index ->
                                    selectedRecommendationIndex = index
                                }
                        )
                    }
                }
            }
        }
    }
}
