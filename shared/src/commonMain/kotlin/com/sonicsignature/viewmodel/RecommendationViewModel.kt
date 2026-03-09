package com.sonicsignature.viewmodel

import com.sonicsignature.engine.RecommendationEngine
import com.sonicsignature.model.IEMRecommendation
import com.sonicsignature.model.MoodVector
import com.sonicsignature.model.SongMetadata
import com.sonicsignature.model.SonicAttributes
import com.sonicsignature.model.TuningPreference
import com.sonicsignature.model.UserSonicProfile
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
        val selectedTracks: List<SongMetadata> = emptyList(),
        val artistChips: List<String> = emptyList(),
        val genreDescription: String = "",
        val selectedBudget: Int = 40000,
        val tuningPreference: TuningPreference = TuningPreference.NEUTRAL,
        val customTuningPreference: String = "",
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
        val current = _state.value.selectedTracks
        if (current.none { it.id == track.id }) {
            _state.value = _state.value.copy(selectedTracks = current + track, error = null)
        }
    }

    fun onTrackRemoved(track: SongMetadata) {
        _state.value =
                _state.value.copy(
                        selectedTracks = _state.value.selectedTracks.filter { it.id != track.id }
                )
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

    fun onBudgetSelected(budget: Int) {
        _state.value = _state.value.copy(selectedBudget = budget)
    }

    fun onTuningSelected(preference: TuningPreference) {
        _state.value = _state.value.copy(tuningPreference = preference)
    }

    fun onCustomTuningPreferenceChanged(preference: String) {
        _state.value = _state.value.copy(customTuningPreference = preference)
    }

    fun getRecommendations() {
        _state.value =
                _state.value.copy(isLoading = true, error = null, recommendations = emptyList())

        scope.launch {
            val current = _state.value
            val result =
                    when (current.inputMode) {
                        InputMode.SONG -> {
                            if (current.selectedTracks.isEmpty()) {
                                return@launch run {
                                    _state.value =
                                            _state.value.copy(
                                                    isLoading = false,
                                                    error = "Please add at least one song."
                                            )
                                }
                            }
                            // Mocking an empty acoustic profile for songs until we fully hook up DB
                            val mockProfile =
                                    UserSonicProfile(
                                            emptyMap(),
                                            SonicAttributes(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f),
                                            emptyList(),
                                            MoodVector.BALANCED,
                                            0L
                                    )
                            engine.recommendFromSongs(
                                    current.selectedTracks,
                                    current.selectedBudget,
                                    current.tuningPreference,
                                    current.customTuningPreference,
                                    mockProfile
                            )
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
                            engine.recommendFromArtists(
                                    current.artistChips,
                                    current.selectedBudget,
                                    current.tuningPreference,
                                    current.customTuningPreference
                            )
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
                                    current.selectedBudget,
                                    current.tuningPreference,
                                    current.customTuningPreference
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
