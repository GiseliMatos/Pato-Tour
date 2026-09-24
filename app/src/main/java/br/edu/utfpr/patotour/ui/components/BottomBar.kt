package br.edu.utfpr.patotour.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import br.edu.utfpr.patotour.R

enum class MainDestination { POINTS, MAP, SETTINGS }

@Composable
fun BottomBar(current: MainDestination, onNavigate: (MainDestination) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        NavigationBarItem(current == MainDestination.POINTS, { onNavigate(MainDestination.POINTS) }, icon = { Text("≡", fontSize = 22.sp) }, label = { Text(stringResource(R.string.points)) })
        NavigationBarItem(current == MainDestination.MAP, { onNavigate(MainDestination.MAP) }, icon = { Text("⌖", fontSize = 22.sp) }, label = { Text(stringResource(R.string.map)) })
        NavigationBarItem(current == MainDestination.SETTINGS, { onNavigate(MainDestination.SETTINGS) }, icon = { Text("⚙", fontSize = 20.sp) }, label = { Text(stringResource(R.string.settings)) })
    }
}
