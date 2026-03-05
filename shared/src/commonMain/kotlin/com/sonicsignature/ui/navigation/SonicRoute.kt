package com.sonicsignature.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class SonicRoute(val label: String, val icon: ImageVector) {
    DISCOVER("Discover", Icons.Default.Search),
    PROFILE("Profile", Icons.Default.AccountCircle),
    COMPARE("Compare", Icons.Default.Info),
    HISTORY("History", Icons.AutoMirrored.Filled.List),
    SETTINGS("Settings", Icons.Default.Settings)
}
