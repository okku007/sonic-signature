package com.sonicsignature.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sonicsignature.api.ConfiguredHttpClient
import com.sonicsignature.api.LLMClientFactory
import com.sonicsignature.api.MusicClient
import com.sonicsignature.engine.RecommendationEngine
import com.sonicsignature.storage.SecureVault
import com.sonicsignature.storage.VaultKeys
import com.sonicsignature.ui.screens.DetailScreen
import com.sonicsignature.ui.screens.DiscoverScreen
import com.sonicsignature.ui.screens.ResultsScreen
import com.sonicsignature.ui.screens.SettingsScreen
import com.sonicsignature.ui.theme.SonicSignatureTheme
import com.sonicsignature.viewmodel.RecommendationViewModel
import com.sonicsignature.viewmodel.SearchViewModel
import com.sonicsignature.viewmodel.SettingsViewModel

private enum class RootRoute {
    DISCOVER,
    RESULTS,
    DETAIL,
    SETTINGS,
}

@Composable
fun SonicSignatureAppRoot(modifier: Modifier = Modifier) {
    val vault = remember { SecureVault() }
    val httpClient = remember { ConfiguredHttpClient.client }

    val settingsViewModel = remember { SettingsViewModel(vault, httpClient) }
    val settingsState by settingsViewModel.state.collectAsState()

    val hasLastFmKey = settingsState.lastFmKeyMasked.isNotEmpty()
    val lastFmKey = remember(hasLastFmKey) {
        if (hasLastFmKey) {
            vault.load(VaultKeys.LASTFM_API_KEY) ?: ""
        } else {
            ""
        }
    }

    val musicClient = remember(lastFmKey) { MusicClient(httpClient, lastFmKey) }
    val llmClientFactory = remember { LLMClientFactory(httpClient, vault) }
    val recommendationEngine = remember(lastFmKey) {
        RecommendationEngine(musicClient, llmClientFactory)
    }
    val searchViewModel = remember(lastFmKey) { SearchViewModel(musicClient) }
    val recommendationViewModel = remember(lastFmKey) {
        RecommendationViewModel(recommendationEngine)
    }

    DisposableEffect(settingsViewModel, searchViewModel, recommendationViewModel) {
        onDispose {
            settingsViewModel.dispose()
            searchViewModel.dispose()
            recommendationViewModel.dispose()
        }
    }

    val searchState by searchViewModel.state.collectAsState()
    val recommendationState by recommendationViewModel.state.collectAsState()
    val hasApiKey = settingsState.apiKeyMasked.isNotEmpty()

    var currentRoute by remember { mutableStateOf(RootRoute.DISCOVER) }
    var selectedDetailIndex by remember { mutableStateOf<Int?>(null) }

    SonicSignatureTheme {
        Surface(modifier = modifier.fillMaxSize(), tonalElevation = 0.dp) {
            Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                when (currentRoute) {
                    RootRoute.DISCOVER -> {
                        DiscoverScreen(
                            searchState = searchState,
                            recommendationState = recommendationState,
                            onQueryChanged = searchViewModel::onQueryChanged,
                            onTrackSelected = { track ->
                                recommendationViewModel.onTrackSelected(track)
                                searchViewModel.clearResults()
                            },
                            onTrackRemoved = recommendationViewModel::onTrackRemoved,
                            onInputModeChanged = recommendationViewModel::onInputModeChanged,
                            onArtistAdded = recommendationViewModel::onArtistAdded,
                            onArtistRemoved = recommendationViewModel::onArtistRemoved,
                            onGenreDescriptionChanged = recommendationViewModel::onGenreDescriptionChanged,
                            onBudgetSelected = recommendationViewModel::onBudgetSelected,
                            onTuningSelected = recommendationViewModel::onTuningSelected,
                            onCustomTuningPreferenceChanged = recommendationViewModel::onCustomTuningPreferenceChanged,
                            onFindMyIEM = {
                                recommendationViewModel.getRecommendations()
                                currentRoute = RootRoute.RESULTS
                            },
                            onNavigateToSettings = { currentRoute = RootRoute.SETTINGS },
                            hasApiKey = hasApiKey,
                        )
                    }

                    RootRoute.RESULTS -> {
                        ResultsScreen(
                            recommendations = recommendationState.recommendations,
                            isLoading = recommendationState.isLoading,
                            error = recommendationState.error,
                            onBack = { currentRoute = RootRoute.DISCOVER },
                            onSearchAgain = {
                                recommendationViewModel.clearResults()
                                currentRoute = RootRoute.DISCOVER
                            },
                            onRecommendationClick = { index ->
                                selectedDetailIndex = index
                                currentRoute = RootRoute.DETAIL
                            },
                        )
                    }

                    RootRoute.DETAIL -> {
                        val recommendation =
                            selectedDetailIndex?.let(recommendationState.recommendations::getOrNull)
                        if (recommendation != null) {
                            DetailScreen(
                                recommendation = recommendation,
                                onBack = { currentRoute = RootRoute.RESULTS },
                            )
                        } else {
                            currentRoute = RootRoute.RESULTS
                        }
                    }

                    RootRoute.SETTINGS -> {
                        SettingsScreen(
                            state = settingsState,
                            onBack = { currentRoute = RootRoute.DISCOVER },
                            onSave = { provider, key, modelId ->
                                settingsViewModel.saveSettings(provider, key, modelId)
                            },
                            onValidate = { provider, key, modelId ->
                                settingsViewModel.validateApiKey(provider, key, modelId)
                            },
                            onValidateLastFmKey = settingsViewModel::validateLastFmKey,
                            onClearAll = settingsViewModel::clearAllData,
                        )
                    }
                }
            }
        }
    }
}
