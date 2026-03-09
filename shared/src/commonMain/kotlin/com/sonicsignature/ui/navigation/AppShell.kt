package com.sonicsignature.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun AppShell(isWideScreen: Boolean, content: @Composable (SonicRoute) -> Unit) {
    var currentRoute by remember { mutableStateOf(SonicRoute.DISCOVER) }

    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            SonicNavigationRail(
                    currentRoute = currentRoute,
                    onRouteSelected = { currentRoute = it }
            )
            Box(modifier = Modifier.weight(1f)) { content(currentRoute) }
        }
    } else {
        Scaffold(
                bottomBar = {
                    SonicBottomNavigation(
                            currentRoute = currentRoute,
                            onRouteSelected = { currentRoute = it }
                    )
                }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) { content(currentRoute) }
        }
    }
}

@Composable
private fun SonicNavigationRail(currentRoute: SonicRoute, onRouteSelected: (SonicRoute) -> Unit) {
    NavigationRail {
        SonicRoute.entries.forEach { route ->
            NavigationRailItem(
                    selected = currentRoute == route,
                    onClick = { onRouteSelected(route) },
                    icon = { Icon(route.icon, contentDescription = route.label) },
                    label = { Text(route.label) }
            )
        }
    }
}

@Composable
private fun SonicBottomNavigation(currentRoute: SonicRoute, onRouteSelected: (SonicRoute) -> Unit) {
    NavigationBar {
        SonicRoute.entries.forEach { route ->
            NavigationBarItem(
                    selected = currentRoute == route,
                    onClick = { onRouteSelected(route) },
                    icon = { Icon(route.icon, contentDescription = route.label) },
                    label = { Text(route.label) }
            )
        }
    }
}
