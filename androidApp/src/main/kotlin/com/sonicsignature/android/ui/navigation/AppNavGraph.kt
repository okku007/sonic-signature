package com.sonicsignature.android.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sonicsignature.android.ui.screens.*
import com.sonicsignature.api.*
import com.sonicsignature.engine.RecommendationEngine
import com.sonicsignature.storage.SecureVault
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
    // For physical device testing, uncomment and set your machine's local IP:
    BackendConfig.baseUrl = "http://192.168.3.250:8080"
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
    val musicClient = remember { MusicClient(httpClient) }
    val llmFactory = remember { LLMClientFactory(httpClient, vault) }
    val engine = remember { RecommendationEngine(musicClient, llmFactory) }

    val searchVM = remember { SearchViewModel(musicClient) }
    val recVM = remember { RecommendationViewModel(engine) }
    val settingsVM = remember { SettingsViewModel(vault, httpClient) }

    val searchState by searchVM.state.collectAsState()
    val recState by recVM.state.collectAsState()
    val settingsState by settingsVM.state.collectAsState()

    val hasApiKey = settingsState.apiKeyMasked.isNotEmpty()

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
                    onClearAll = settingsVM::clearAllData
            )
        }
    }
}
