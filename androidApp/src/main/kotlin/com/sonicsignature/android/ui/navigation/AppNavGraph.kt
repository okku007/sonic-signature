package com.sonicsignature.android.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sonicsignature.android.ui.screens.*
import com.sonicsignature.api.*
import com.sonicsignature.engine.RecommendationEngine
import com.sonicsignature.storage.SecureVault
import com.sonicsignature.storage.VaultKeys
import com.sonicsignature.viewmodel.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private object Routes {
    const val HOME = "home"
    const val RESULTS = "results"
    const val DETAIL = "detail/{index}"
    const val SETTINGS = "settings"
    fun detail(index: Int) = "detail/$index"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    // ── Dependency wiring ────────────────────────────────────────────────────
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

    // Read Last.fm key from vault → pass to MusicClient
    val lastFmKey = vault.load(VaultKeys.LASTFM_API_KEY) ?: ""
    val musicClient = remember(lastFmKey) { MusicClient(httpClient, lastFmKey) }
    val llmFactory = remember { LLMClientFactory(httpClient, vault) }
    val engine = remember(lastFmKey) { RecommendationEngine(musicClient, llmFactory) }

    val searchVM = remember(lastFmKey) { SearchViewModel(musicClient) }
    val recVM = remember(lastFmKey) { RecommendationViewModel(engine) }

    val searchState by searchVM.state.collectAsState()
    val recState by recVM.state.collectAsState()

    val hasApiKey = settingsState.apiKeyMasked.isNotEmpty()
    val hasLastFmKey = settingsState.lastFmKeyMasked.isNotEmpty()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
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
                    onFindMyIEM = {
                        recVM.getRecommendations()
                        navController.navigate(Routes.RESULTS)
                    },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                    hasApiKey = hasApiKey
            )
        }

        composable(Routes.RESULTS) {
            ResultsScreen(
                    recommendations = recState.recommendations,
                    isLoading = recState.isLoading,
                    error = recState.error,
                    onBack = { navController.popBackStack() },
                    onSearchAgain = {
                        recVM.clearResults()
                        navController.popBackStack()
                    },
                    onRecommendationClick = { index ->
                        navController.navigate(Routes.detail(index))
                    }
            )
        }

        composable(Routes.DETAIL) { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
            val recommendation = recState.recommendations.getOrNull(index)
            if (recommendation != null) {
                DetailScreen(
                        recommendation = recommendation,
                        onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                    state = settingsState,
                    onBack = { navController.popBackStack() },
                    onSave = { provider, key, modelId ->
                        settingsVM.saveSettings(provider, key, modelId)
                    },
                    onValidate = { provider, key, modelId ->
                        settingsVM.validateApiKey(provider, key, modelId)
                    },
                    onSaveLastFmKey = settingsVM::saveLastFmKey,
                    onValidateLastFmKey = settingsVM::validateLastFmKey,
                    onClearAll = settingsVM::clearAllData
            )
        }
    }
}
