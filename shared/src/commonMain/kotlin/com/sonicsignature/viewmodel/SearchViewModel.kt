package com.sonicsignature.viewmodel

import com.sonicsignature.api.SpotifyClient
import com.sonicsignature.model.SongMetadata
import com.sonicsignature.util.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SearchUiState(
    val query: String = "",
    val results: List<SongMetadata> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the song search autocomplete.
 * Debounces input by 300ms to reduce Spotify API calls (T-103).
 * Constitution §4.4: Network calls on background dispatcher.
 */
class SearchViewModel(
    private val spotifyClient: SpotifyClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var debounceJob: Job? = null

    fun onQueryChanged(query: String) {
        _state.value = _state.value.copy(query = query, error = null)

        debounceJob?.cancel()
        if (query.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), isLoading = false)
            return
        }

        debounceJob = scope.launch {
            delay(300)
            _state.value = _state.value.copy(isLoading = true)

            when (val result = spotifyClient.searchTracks(query)) {
                is Result.Success -> _state.value = _state.value.copy(
                    results = result.data,
                    isLoading = false,
                    error = null
                )
                is Result.Error -> _state.value = _state.value.copy(
                    results = emptyList(),
                    isLoading = false,
                    error = result.message
                )
                Result.Loading -> Unit
            }
        }
    }

    fun clearResults() {
        _state.value = SearchUiState()
        debounceJob?.cancel()
    }

    fun dispose() {
        scope.cancel()
    }
}
