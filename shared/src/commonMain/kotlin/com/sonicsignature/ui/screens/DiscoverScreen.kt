package com.sonicsignature.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sonicsignature.model.SongMetadata
import com.sonicsignature.model.TuningPreference
import com.sonicsignature.ui.components.BudgetSlider
import com.sonicsignature.ui.components.SonicBadge
import com.sonicsignature.ui.components.SonicBlockGap
import com.sonicsignature.ui.components.SonicBottomNav
import com.sonicsignature.ui.components.SonicContentColumn
import com.sonicsignature.ui.components.SonicDivider
import com.sonicsignature.ui.components.SonicField
import com.sonicsignature.ui.components.SonicLoadingState
import com.sonicsignature.ui.components.SonicPanel
import com.sonicsignature.ui.components.SonicPrimaryButton
import com.sonicsignature.ui.components.SonicScreenHeader
import com.sonicsignature.ui.components.SonicSecondaryButton
import com.sonicsignature.ui.components.SonicSectionLabel
import com.sonicsignature.ui.components.SonicStatusMessage
import com.sonicsignature.ui.components.SonicTopLevelDestination
import com.sonicsignature.ui.components.SonicTone
import com.sonicsignature.ui.util.rememberKeyboardVisible
import com.sonicsignature.viewmodel.InputMode
import com.sonicsignature.viewmodel.RecommendationUiState
import com.sonicsignature.viewmodel.SearchUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
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
    var tuningExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isKeyboardVisible = rememberKeyboardVisible()
    val ctaText =
            if (hasApiKey) {
                "INITIATE SEARCH PROTOCOL"
            } else {
                "CONFIGURE AI KEY"
            }

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .imePadding()
    ) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                                .padding(bottom = if (isKeyboardVisible) 24.dp else 176.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SonicContentColumn {
                SonicScreenHeader(
                        title = "DISCOVER",
                        subtitle =
                                "Enter reference songs, artists, or moods to calibrate the current recommendation engine."
                )

                SonicPanel(
                        title = "Input protocol",
                        badge = recommendationState.inputMode.name.replace('_', ' ')
                ) {
                    InputModeTabs(
                            selectedTab = selectedTab,
                            onTabSelected = { index ->
                                onInputModeChanged(
                                        when (index) {
                                            0 -> InputMode.SONG
                                            1 -> InputMode.ARTIST
                                            else -> InputMode.GENRE
                                        }
                                )
                            }
                    )

                    when (recommendationState.inputMode) {
                        InputMode.SONG ->
                                SongSearchInput(
                                        searchState = searchState,
                                        onQueryChanged = onQueryChanged,
                                        onTrackSelected = onTrackSelected,
                                        onTrackRemoved = onTrackRemoved,
                                        selectedTracks = recommendationState.selectedTracks,
                                        scope = scope
                                )
                        InputMode.ARTIST ->
                                ArtistSearchInput(
                                        artists = recommendationState.artistChips,
                                        onArtistAdded = onArtistAdded,
                                        onArtistRemoved = onArtistRemoved,
                                        scope = scope
                                )
                        InputMode.GENRE ->
                                GenreMoodInput(
                                        description = recommendationState.genreDescription,
                                        onDescriptionChanged = onGenreDescriptionChanged,
                                        scope = scope
                                )
                    }
                }

                SonicPanel(
                        title = "Calibration module",
                        badge = "Budget / tuning",
                        emphasized = true
                ) {
                    BudgetSlider(
                            value = sliderValue,
                            onValueChange = {
                                sliderValue = it
                                onBudgetSelected(it.toInt())
                            }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SonicSectionLabel("Tuning bias")
                        Box {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                SonicSecondaryButton(
                                        text =
                                                "Target signature: ${recommendationState.tuningPreference.displayName}",
                                        onClick = { tuningExpanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                        text =
                                                "Choose from preset tuning biases or Custom for your own description.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                    expanded = tuningExpanded,
                                    onDismissRequest = { tuningExpanded = false }
                            ) {
                                TuningPreference.entries.forEach { preference ->
                                    DropdownMenuItem(
                                            text = { Text(preference.displayName) },
                                            onClick = {
                                                onTuningSelected(preference)
                                                tuningExpanded = false
                                            }
                                    )
                                }
                            }
                        }
                    }

                    if (recommendationState.tuningPreference == TuningPreference.CUSTOM) {
                        SonicField(
                                value = recommendationState.customTuningPreference,
                                onValueChange = onCustomTuningPreferenceChanged,
                                label = "Custom tuning description",
                                placeholder = "e.g. sub-bass lift with restrained upper mids",
                                modifier = Modifier.discoverBringIntoView(scope = scope),
                                singleLine = false,
                                maxLines = 3
                        )
                    }
                }

                InputPreviewPanel(
                        recommendationState = recommendationState,
                        hasApiKey = hasApiKey
                )

                searchState.error?.let { error ->
                    SonicStatusMessage(
                            title = "Search issue",
                            body = error,
                            tone = SonicTone.Warning
                    )
                }

                recommendationState.error?.let { error ->
                    SonicStatusMessage(
                            title = "Recommendation blocked",
                            body = error,
                            tone = SonicTone.Danger
                    )
                }

                if (!hasApiKey) {
                    SonicStatusMessage(
                            title = "AI key required",
                            body =
                                    "Recommendations stay disabled until you validate a provider key in Settings.",
                            tone = SonicTone.Warning
                    )
                }
            }
        }

        Box(
                modifier =
                        Modifier.align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                        visible = !isKeyboardVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 })
                ) {
                    SonicContentColumn(
                            modifier =
                                    Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (recommendationState.isLoading) {
                            SonicLoadingState(compact = true)
                        }

                        SonicPrimaryButton(
                                text = ctaText,
                                onClick = if (hasApiKey) onFindMyIEM else onNavigateToSettings,
                                enabled = !recommendationState.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                leading = {
                                    Icon(
                                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                        )
                    }
                }

                AnimatedVisibility(
                        visible = !isKeyboardVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 })
                ) {
                    SonicBottomNav(
                            selected = SonicTopLevelDestination.Discover,
                            onDiscoverClick = {},
                            onSettingsClick = onNavigateToSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun InputModeTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Song", "Artist", "Genre")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        tabs.forEachIndexed { index, title ->
            SegmentedButton(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                    colors =
                            SegmentedButtonDefaults.colors(
                                    activeContainerColor =
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                    alpha = 0.18f
                                            ),
                                    activeContentColor = MaterialTheme.colorScheme.primary,
                                    inactiveContainerColor =
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                    alpha = 0.62f
                                            ),
                                    inactiveContentColor =
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                    activeBorderColor = Color.Transparent,
                                    inactiveBorderColor = Color.Transparent
                            ),
                    label = { Text(title) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SongSearchInput(
        searchState: SearchUiState,
        onQueryChanged: (String) -> Unit,
        onTrackSelected: (SongMetadata) -> Unit,
        onTrackRemoved: (SongMetadata) -> Unit,
        selectedTracks: List<SongMetadata>,
        scope: kotlinx.coroutines.CoroutineScope
) {
    Column(verticalArrangement = Arrangement.spacedBy(SonicBlockGap)) {
        SonicField(
                value = searchState.query,
                onValueChange = onQueryChanged,
                label = "Reference audio",
                placeholder = "Search tracks from Last.fm",
                modifier = Modifier.discoverBringIntoView(scope = scope),
                leading = {
                    Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailing =
                        if (searchState.isLoading) {
                            {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                )
                            }
                        } else {
                            null
                        },
                supportingText =
                        "Selected tracks become the real input set for recommendation generation."
        )

        if (selectedTracks.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SonicSectionLabel("Selected tracks")
                FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedTracks.forEach { track ->
                        SelectionChip(
                                label = "${track.name} - ${track.artist}",
                                onRemove = { onTrackRemoved(track) }
                        )
                    }
                }
            }
        }

        if (searchState.results.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SonicDivider()
                SonicSectionLabel("Live matches")
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.12f
                                                ),
                                                RoundedCornerShape(22.dp)
                                        )
                ) {
                    searchState.results.take(6).forEachIndexed { index, track ->
                        SearchResultRow(
                                track = track,
                                onTrackSelected = { onTrackSelected(track) }
                        )
                        if (index < searchState.results.take(6).lastIndex) {
                            SonicDivider()
                        }
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
        onArtistRemoved: (String) -> Unit,
        scope: kotlinx.coroutines.CoroutineScope
) {
    var artistInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(SonicBlockGap)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SonicField(
                        value = artistInput,
                        onValueChange = { artistInput = it },
                        label = "Reference artists",
                        placeholder = "Add one or more artists",
                        modifier = Modifier.discoverBringIntoView(scope = scope),
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

                SonicSecondaryButton(
                        text = "Add",
                        onClick = {
                            if (artistInput.isNotBlank()) {
                                onArtistAdded(artistInput)
                                artistInput = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leading = {
                            Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                )
            }
        }

        if (artists.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SonicSectionLabel("Calibration pool")
                FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    artists.forEach { artist ->
                        SelectionChip(label = artist, onRemove = { onArtistRemoved(artist) })
                    }
                }
            }
        }
    }
}

@Composable
private fun GenreMoodInput(
        description: String,
        onDescriptionChanged: (String) -> Unit,
        scope: kotlinx.coroutines.CoroutineScope
) {
    SonicField(
            value = description,
            onValueChange = onDescriptionChanged,
            label = "Genre or mood brief",
            placeholder = "e.g. dark ambient, spacious post-rock, fast technical metal",
            modifier = Modifier.discoverBringIntoView(scope = scope),
            singleLine = false,
            maxLines = 5,
            supportingText =
                    "This description is sent directly into the current genre recommendation path."
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InputPreviewPanel(recommendationState: RecommendationUiState, hasApiKey: Boolean) {
    val inputPreview =
            when (recommendationState.inputMode) {
                InputMode.SONG ->
                        if (recommendationState.selectedTracks.isEmpty()) {
                            "No tracks armed yet."
                        } else {
                            recommendationState.selectedTracks.joinToString(separator = "\n") {
                                "${it.name} - ${it.artist}"
                            }
                        }
                InputMode.ARTIST ->
                        if (recommendationState.artistChips.isEmpty()) {
                            "No artists added yet."
                        } else {
                            recommendationState.artistChips.joinToString(", ")
                        }
                InputMode.GENRE ->
                        recommendationState.genreDescription.ifBlank {
                            "No genre brief entered yet."
                        }
            }

    SonicPanel(title = "Active calibration", badge = if (hasApiKey) "Ready" else "Blocked") {
        FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SonicBadge(text = recommendationState.inputMode.name, tone = SonicTone.Accent)
            SonicBadge(
                    text = "₹${recommendationState.selectedBudget}",
                    tone = SonicTone.Neutral
            )
            SonicBadge(
                    text = recommendationState.tuningPreference.displayName,
                    tone = SonicTone.Neutral
            )
            if (recommendationState.tuningPreference == TuningPreference.CUSTOM &&
                            recommendationState.customTuningPreference.isNotBlank()
            ) {
                SonicBadge(text = "Custom note", tone = SonicTone.Warning)
            }
        }

        Text(
                text = inputPreview,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
        )

        if (recommendationState.tuningPreference == TuningPreference.CUSTOM &&
                        recommendationState.customTuningPreference.isNotBlank()
        ) {
            Text(
                    text = recommendationState.customTuningPreference,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchResultRow(track: SongMetadata, onTrackSelected: () -> Unit) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clickable(onClick = onTrackSelected)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = track.name, style = MaterialTheme.typography.titleLarge)
            Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SonicBadge(text = "Add", tone = SonicTone.Accent)
    }
}

@Composable
private fun SelectionChip(label: String, onRemove: () -> Unit) {
    Row(
            modifier =
                    Modifier.background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.14f),
                                    RoundedCornerShape(999.dp)
                            )
                            .padding(start = 12.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Box(
                modifier =
                        Modifier.size(18.dp)
                                .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                )
                                .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.discoverBringIntoView(scope: kotlinx.coroutines.CoroutineScope): Modifier {
    val bringIntoViewRequester = BringIntoViewRequester()
    return this.bringIntoViewRequester(bringIntoViewRequester).onFocusChanged { state ->
        if (state.isFocused) {
            scope.launch {
                delay(250)
                bringIntoViewRequester.bringIntoView()
                delay(250)
                bringIntoViewRequester.bringIntoView()
            }
        }
    }
}
