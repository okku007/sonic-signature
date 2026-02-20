package com.sonicsignature.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.sonicsignature.model.BudgetTier
import com.sonicsignature.model.SongMetadata
import com.sonicsignature.viewmodel.InputMode
import com.sonicsignature.viewmodel.RecommendationUiState
import com.sonicsignature.viewmodel.SearchUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
        searchState: SearchUiState,
        recommendationState: RecommendationUiState,
        onQueryChanged: (String) -> Unit,
        onTrackSelected: (SongMetadata) -> Unit,
        onTrackRemoved: (SongMetadata) -> Unit,
        onInputModeChanged: (InputMode) -> Unit,
        onArtistAdded: (String) -> Unit,
        onArtistRemoved: (String) -> Unit,
        onGenreDescriptionChanged: (String) -> Unit,
        onBudgetSelected: (BudgetTier) -> Unit,
        onFindMyIEM: () -> Unit,
        onNavigateToSettings: () -> Unit,
        hasApiKey: Boolean
) {
    val tabs = listOf("Song Search", "Artist Search", "Genre / Mood")
    val selectedTab =
            when (recommendationState.inputMode) {
                InputMode.SONG -> 0
                InputMode.ARTIST -> 1
                InputMode.GENRE -> 2
            }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Text("Sonic Signature", style = MaterialTheme.typography.titleLarge)
                        },
                        actions = {
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }
                )
            }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── Tab Row ──────────────────────────────────────────────────────
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                            selected = selectedTab == index,
                            onClick = {
                                onInputModeChanged(
                                        when (index) {
                                            0 -> InputMode.SONG
                                            1 -> InputMode.ARTIST
                                            else -> InputMode.GENRE
                                        }
                                )
                            },
                            text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            // ── Tab Content ──────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                when (recommendationState.inputMode) {
                    InputMode.SONG ->
                            SongSearchTab(
                                    searchState = searchState,
                                    onQueryChanged = onQueryChanged,
                                    onTrackSelected = onTrackSelected,
                                    onTrackRemoved = onTrackRemoved,
                                    selectedTracks = recommendationState.selectedTracks
                            )
                    InputMode.ARTIST ->
                            ArtistSearchTab(
                                    artists = recommendationState.artistChips,
                                    onArtistAdded = onArtistAdded,
                                    onArtistRemoved = onArtistRemoved
                            )
                    InputMode.GENRE ->
                            GenreMoodTab(
                                    description = recommendationState.genreDescription,
                                    onDescriptionChanged = onGenreDescriptionChanged
                            )
                }
            }

            // ── Budget Selector ──────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Budget", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BudgetTier.entries.forEach { tier ->
                        FilterChip(
                                selected = recommendationState.selectedBudget == tier,
                                onClick = { onBudgetSelected(tier) },
                                label = {
                                    Text(tier.label, style = MaterialTheme.typography.labelSmall)
                                }
                        )
                    }
                }
            }

            // ── CTA Button ───────────────────────────────────────────────────
            Button(
                    onClick = onFindMyIEM,
                    enabled = hasApiKey && !recommendationState.isLoading,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
            ) {
                if (recommendationState.isLoading) {
                    // Cycle through loading messages to keep user engaged
                    var loadingPhase by remember { mutableStateOf(0) }
                    val loadingMessages =
                            listOf(
                                    "Analyzing audio profile…",
                                    "Consulting the audiophile oracle…",
                                    "Filtering by your budget…",
                                    "Scanning 1,000+ driver configurations…",
                                    "Evaluating sound signatures…",
                                    "Checking Crinacle's ranking database…",
                                    "Synthesizing technical reviews…",
                                    "Calibrating final matches…",
                                    "Finding your perfect sonic match…",
                                    "Almost there…"
                            )

                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(2500)
                            if (loadingPhase < loadingMessages.lastIndex) {
                                loadingPhase++
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                                loadingMessages[loadingPhase],
                                style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Text(
                            if (hasApiKey) "Find My IEM" else "Set Up API Key in Settings",
                            style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // ── Error ────────────────────────────────────────────────────────
            recommendationState.error?.let { error ->
                Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
                )
            }
        }
    }
}

// ── Song Search Tab ────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SongSearchTab(
        searchState: SearchUiState,
        onQueryChanged: (String) -> Unit,
        onTrackSelected: (SongMetadata) -> Unit,
        onTrackRemoved: (SongMetadata) -> Unit,
        selectedTracks: List<SongMetadata>
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
                value = searchState.query,
                onValueChange = onQueryChanged,
                label = { Text("Search for a song") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchState.isLoading) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
        )

        // Selected songs as removable chips
        if (selectedTracks.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedTracks.forEach { track ->
                    InputChip(
                            selected = true,
                            onClick = {},
                            label = { Text("${track.name} — ${track.artist}") },
                            trailingIcon = {
                                Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove ${track.name}",
                                        modifier =
                                                Modifier.size(16.dp).clickable {
                                                    onTrackRemoved(track)
                                                }
                                )
                            }
                    )
                }
            }
        }

        // Search results dropdown
        if (searchState.results.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                LazyColumn {
                    items(searchState.results) { track ->
                        ListItem(
                                headlineContent = { Text(track.name) },
                                supportingContent = { Text(track.artist) },
                                modifier = Modifier.clickable { onTrackSelected(track) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// ── Artist Search Tab ──────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArtistSearchTab(
        artists: List<String>,
        onArtistAdded: (String) -> Unit,
        onArtistRemoved: (String) -> Unit
) {
    var artistInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
                "Add artists that define your taste",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                    value = artistInput,
                    onValueChange = { artistInput = it },
                    label = { Text("Artist name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions =
                            KeyboardActions(
                                    onDone = {
                                        if (artistInput.isNotBlank()) {
                                            onArtistAdded(artistInput)
                                            artistInput = ""
                                        }
                                    }
                            )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                    onClick = {
                        if (artistInput.isNotBlank()) {
                            onArtistAdded(artistInput)
                            artistInput = ""
                        }
                    }
            ) { Icon(Icons.Default.Add, contentDescription = "Add artist") }
        }

        Spacer(Modifier.height(12.dp))

        // Artist chips
        if (artists.isNotEmpty()) {
            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                artists.forEach { artist ->
                    InputChip(
                            selected = true,
                            onClick = {},
                            label = { Text(artist) },
                            trailingIcon = {
                                Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove $artist",
                                        modifier =
                                                Modifier.size(16.dp).clickable {
                                                    onArtistRemoved(artist)
                                                }
                                )
                            }
                    )
                }
            }
        }
    }
}

// ── Genre / Mood Tab ───────────────────────────────────────────────────────────

@Composable
private fun GenreMoodTab(description: String, onDescriptionChanged: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
                "Describe your genre or mood",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChanged,
                label = { Text("e.g. dark ambient, fast metal, lo-fi hip-hop") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
        )
    }
}
