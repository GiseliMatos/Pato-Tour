package br.edu.utfpr.patotour.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import br.edu.utfpr.patotour.R

enum class MainDestination { POINTS, MAP, SETTINGS }

@Composable
fun BottomBar(current: MainDestination, onNavigate: (MainDestination) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        NavigationBarItem(
            selected = current == MainDestination.POINTS,
            onClick = { onNavigate(MainDestination.POINTS) },
            icon = {
                Icon(
                    if (current == MainDestination.POINTS) Icons.Filled.Place else Icons.Outlined.Place,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.points)) }
        )
        NavigationBarItem(
            selected = current == MainDestination.MAP,
            onClick = { onNavigate(MainDestination.MAP) },
            icon = {
                Icon(
                    if (current == MainDestination.MAP) Icons.Filled.Map else Icons.Outlined.Map,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.map)) }
        )
        NavigationBarItem(
            selected = current == MainDestination.SETTINGS,
            onClick = { onNavigate(MainDestination.SETTINGS) },
            icon = {
                Icon(
                    if (current == MainDestination.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.settings)) }
        )
    }
}

@Composable
fun BottomBar(
    pointsSelected: Boolean,
    settingsSelected: Boolean,
    onPointsClick: () -> Unit,
    onMapClick: () -> Unit = {}
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        NavigationBarItem(
            selected = pointsSelected,
            onClick = onPointsClick,
            icon = { Icon(if (pointsSelected) Icons.Filled.Place else Icons.Outlined.Place, contentDescription = null) },
            label = { Text(stringResource(R.string.points)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onMapClick,
            icon = { Icon(Icons.Outlined.Map, contentDescription = null) },
            label = { Text(stringResource(R.string.map)) }
        )
        NavigationBarItem(
            selected = settingsSelected,
            onClick = {},
            icon = { Icon(if (settingsSelected) Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.settings)) }
        )
    }
}