package com.sonicsignature.viewmodel

import com.sonicsignature.engine.RecommendationEngine
import com.sonicsignature.model.BudgetTier
import com.sonicsignature.model.IEMRecommendation
import com.sonicsignature.model.SongMetadata
import com.sonicsignature.util.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class InputMode {
    SONG,
    ARTIST,
    GENRE
}

data class RecommendationUiState(
        val inputMode: InputMode = InputMode.SONG,
        val selectedTrack: SongMetadata? = null,
        val artistChips: List<String> = emptyList(),
        val genreDescription: String = "",
        val selectedBudget: BudgetTier = BudgetTier.MID_RANGE,
        val recommendations: List<IEMRecommendation> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
)

/**
 * ViewModel for the recommendation flow. Handles all three input modes: song, artist list, and
 * genre/mood. Constitution §4.4: Engine calls run on background dispatcher.
 */
class RecommendationViewModel(
        private val engine: RecommendationEngine,
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _state = MutableStateFlow(RecommendationUiState())
    val state: StateFlow<RecommendationUiState> = _state.asStateFlow()

    fun onInputModeChanged(mode: InputMode) {
        _state.value = _state.value.copy(inputMode = mode, error = null)
    }

    fun onTrackSelected(track: SongMetadata) {
        _state.value = _state.value.copy(selectedTrack = track, error = null)
    }

    fun onArtistAdded(artist: String) {
        if (artist.isBlank()) return
        _state.value = _state.value.copy(artistChips = _state.value.artistChips + artist.trim())
    }

    fun onArtistRemoved(artist: String) {
        _state.value = _state.value.copy(artistChips = _state.value.artistChips - artist)
    }

    fun onGenreDescriptionChanged(description: String) {
        _state.value = _state.value.copy(genreDescription = description, error = null)
    }

    fun onBudgetSelected(tier: BudgetTier) {
        _state.value = _state.value.copy(selectedBudget = tier)
    }

    fun getRecommendations() {
        _state.value =
                _state.value.copy(isLoading = true, error = null, recommendations = emptyList())

        scope.launch {
            val current = _state.value
            val result =
                    when (current.inputMode) {
                        InputMode.SONG -> {
                            val track =
                                    current.selectedTrack
                                            ?: return@launch run {
                                                _state.value =
                                                        _state.value.copy(
                                                                isLoading = false,
                                                                error =
                                                                        "Please select a song first."
                                                        )
                                            }
                            engine.recommendFromSong(track, current.selectedBudget)
                        }
                        InputMode.ARTIST -> {
                            if (current.artistChips.isEmpty()) {
                                return@launch run {
                                    _state.value =
                                            _state.value.copy(
                                                    isLoading = false,
                                                    error = "Please add at least one artist."
                                            )
                                }
                            }
                            engine.recommendFromArtists(current.artistChips, current.selectedBudget)
                        }
                        InputMode.GENRE -> {
                            if (current.genreDescription.isBlank()) {
                                return@launch run {
                                    _state.value =
                                            _state.value.copy(
                                                    isLoading = false,
                                                    error = "Please describe your genre or mood."
                                            )
                                }
                            }
                            engine.recommendFromGenre(
                                    current.genreDescription,
                                    current.selectedBudget
                            )
                        }
                    }

            _state.value =
                    when (result) {
                        is Result.Success ->
                                _state.value.copy(
                                        recommendations = result.data,
                                        isLoading = false,
                                        error = null
                                )
                        is Result.Error ->
                                _state.value.copy(isLoading = false, error = result.message)
                        Result.Loading -> _state.value
                    }
        }
    }

    fun clearResults() {
        _state.value = _state.value.copy(recommendations = emptyList(), error = null)
    }

    fun dispose() {
        scope.cancel()
    }
}
