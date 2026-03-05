package com.sonicsignature.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.sonicsignature.model.SongMetadata
import com.sonicsignature.model.TuningPreference
import com.sonicsignature.ui.components.BudgetSlider
import com.sonicsignature.viewmodel.InputMode
import com.sonicsignature.viewmodel.RecommendationUiState
import com.sonicsignature.viewmodel.SearchUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
        searchState: SearchUiState,
        recommendationState: RecommendationUiState,
        onQueryChanged: (String) -> Unit,
        onTrackSelected: (SongMetadata) -> Unit,
        onTrackRemoved: (SongMetadata) -> Unit,
        onInputModeChanged: (InputMode) -> Unit,
        onArtistAdded: (String) -> Unit,
        onArtistRemoved: (String) -> Unit,
        onGenreDescriptionChanged: (String) -> Unit,
        onBudgetSelected: (Int) -> Unit,
        onTuningSelected: (TuningPreference) -> Unit,
        onCustomTuningPreferenceChanged: (String) -> Unit,
        onFindMyIEM: () -> Unit,
        onNavigateToSettings: () -> Unit,
        hasApiKey: Boolean
) {
    val tabs = listOf("Song", "Artist", "Genre")
    val selectedTab =
            when (recommendationState.inputMode) {
                InputMode.SONG -> 0
                InputMode.ARTIST -> 1
                InputMode.GENRE -> 2
            }

    var sliderValue by
            remember(recommendationState.selectedBudget) {
                mutableStateOf(recommendationState.selectedBudget.toFloat())
            }

    // We mock the currently selected tuning for UI scaffolding if it's not in the ViewModel yet.
    // Ultimately the ViewModel should hold this state.
    var currentTuning by remember { mutableStateOf(TuningPreference.NEUTRAL) }
    var tuningExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().imePadding().padding(16.dp)) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Discover", style = MaterialTheme.typography.headlineLarge)
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

        // Scrollable Body
        Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                // Tab Selection
                TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = MaterialTheme.colorScheme.primary
                            )
                        }
                ) {
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

                Spacer(Modifier.height(16.dp))

                // Input Content
                when (recommendationState.inputMode) {
                    InputMode.SONG ->
                            SongSearchInput(
                                    searchState = searchState,
                                    onQueryChanged = onQueryChanged,
                                    onTrackSelected = onTrackSelected,
                                    onTrackRemoved = onTrackRemoved,
                                    selectedTracks = recommendationState.selectedTracks
                            )
                    InputMode.ARTIST ->
                            ArtistSearchInput(
                                    artists = recommendationState.artistChips,
                                    onArtistAdded = onArtistAdded,
                                    onArtistRemoved = onArtistRemoved
                            )
                    InputMode.GENRE ->
                            GenreMoodInput(
                                    description = recommendationState.genreDescription,
                                    onDescriptionChanged = onGenreDescriptionChanged
                            )
                }

                Spacer(Modifier.height(24.dp))

                // Budget Slider
                BudgetSlider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = it
                            onBudgetSelected(it.toInt())
                        },
                        modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Tuning Preference Toggle
                ExposedDropdownMenuBox(
                        expanded = tuningExpanded,
                        onExpandedChange = { tuningExpanded = it }
                ) {
                    OutlinedTextField(
                            value = "Tuning Bias: ${currentTuning.displayName}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tuning Preference") },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            leadingIcon = null,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = tuningExpanded)
                            },
                            singleLine = true,
                    )
                    ExposedDropdownMenu(
                            expanded = tuningExpanded,
                            onDismissRequest = { tuningExpanded = false }
                    ) {
                        TuningPreference.entries.forEach { preference ->
                            DropdownMenuItem(
                                    text = { Text(preference.displayName) },
                                    onClick = {
                                        currentTuning = preference
                                        onTuningSelected(preference)
                                        tuningExpanded = false
                                    }
                            )
                        }
                    }
                }

                if (currentTuning == TuningPreference.CUSTOM) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                            value = recommendationState.customTuningPreference,
                            onValueChange = onCustomTuningPreferenceChanged,
                            label = { Text("Describe Tuning Preference") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                    )
                }
            }
        }

        // CTA Button
        Button(
                onClick = onFindMyIEM,
                enabled = hasApiKey && !recommendationState.isLoading,
                modifier = Modifier.padding(bottom = 16.dp)
        ) {
            if (recommendationState.isLoading) {
                SonicLoadingAnimation()
            } else {
                Text(if (hasApiKey) "Find My IEM" else "Set Up API Key in Settings")
            }
        }

        // Error Message
        recommendationState.error?.let { error ->
            Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun SonicLoadingAnimation() {
    var loadingPhase by remember { mutableStateOf(0) }
    val loadingMessages =
            listOf(
                    "Analyzing audio profile…",
                    "Consulting the audiophile oracle…",
                    "Scanning 1,000+ driver configs…",
                    "Checking Crinacle's database…",
                    "Calibrating final matches…"
            )

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            if (loadingPhase < loadingMessages.lastIndex) {
                loadingPhase++
            } else {
                loadingPhase = 0
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = androidx.compose.ui.graphics.Color.White
        )
        Spacer(Modifier.width(12.dp))
        Text(
                loadingMessages[loadingPhase],
                style = MaterialTheme.typography.labelLarge,
                color = androidx.compose.ui.graphics.Color.White
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SongSearchInput(
        searchState: SearchUiState,
        onQueryChanged: (String) -> Unit,
        onTrackSelected: (SongMetadata) -> Unit,
        onTrackRemoved: (SongMetadata) -> Unit,
        selectedTracks: List<SongMetadata>
) {
    Column {
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
                }
        )

        if (selectedTracks.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedTracks.forEach { track ->
                    AssistChip(
                            onClick = {},
                            label = { Text("${track.name} — ${track.artist}") },
                            trailingIcon = {
                                Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
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

        if (searchState.results.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.weight(1f, fill = false).heightIn(max = 200.dp)) {
                LazyColumn {
                    items(searchState.results) { track ->
                        ListItem(
                                headlineContent = { Text(track.name) },
                                supportingContent = { Text(track.artist) },
                                colors =
                                        ListItemDefaults.colors(
                                                containerColor =
                                                        androidx.compose.ui.graphics.Color
                                                                .Transparent
                                        ),
                                modifier = Modifier.clickable { onTrackSelected(track) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArtistSearchInput(
        artists: List<String>,
        onArtistAdded: (String) -> Unit,
        onArtistRemoved: (String) -> Unit
) {
    var artistInput by remember { mutableStateOf("") }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                    value = artistInput,
                    onValueChange = { artistInput = it },
                    label = { Text("Artist name") },
                    modifier = Modifier.weight(1f),
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
            ) {
                Icon(
                        Icons.Default.Add,
                        contentDescription = "Add artist",
                        tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (artists.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                artists.forEach { artist ->
                    AssistChip(
                            onClick = {},
                            label = { Text(artist) },
                            trailingIcon = {
                                Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
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

@Composable
private fun GenreMoodInput(description: String, onDescriptionChanged: (String) -> Unit) {
    OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            label = { Text("e.g. dark ambient, fast metal") },
            singleLine = false,
            maxLines = 5,
            modifier = Modifier.height(120.dp).fillMaxWidth()
    )
}
