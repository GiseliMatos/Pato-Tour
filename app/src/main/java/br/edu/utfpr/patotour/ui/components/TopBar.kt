package br.edu.utfpr.patotour.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopBar(title: String, back: (() -> Unit)? = null) {
    Surface(shadowElevation = 2.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (back != null) {
                Text(
                    "‹",
                    fontSize = 40.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp).clickable { back() }
                )
            } else {
                Text("☰", fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(40.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            if (back == null) Text("●", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            else Spacer(Modifier.width(40.dp))
        }
    }
}
