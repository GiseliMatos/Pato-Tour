package br.edu.utfpr.patotour.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import br.edu.utfpr.patotour.R

@Composable
fun BottomBar() {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        NavigationBarItem(true, {}, icon = { Text("≡", fontSize = 22.sp) }, label = { Text(stringResource(R.string.points)) })
        NavigationBarItem(false, {}, icon = { Text("⌖", fontSize = 22.sp) }, label = { Text(stringResource(R.string.map)) })
        NavigationBarItem(false, {}, icon = { Text("⚙", fontSize = 20.sp) }, label = { Text(stringResource(R.string.settings)) })
    }
}