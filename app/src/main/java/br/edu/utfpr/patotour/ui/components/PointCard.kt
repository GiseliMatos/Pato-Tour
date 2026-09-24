package br.edu.utfpr.patotour.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.patotour.R
import br.edu.utfpr.patotour.data.model.PontoTuristico

@Composable
fun PointCard(point: PontoTuristico, onEdit: () -> Unit, onViewOnMap: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            TouristPointImage(point.caminhoImagem, Modifier.fillMaxWidth().height(210.dp), point.nome)
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(point.nome, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Normal)
                Text("⌖ ${point.latitude}, ${point.longitude}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
                if (point.enderecoTextual.isNotBlank()) Text(point.enderecoTextual, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                HorizontalDivider(Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onViewOnMap) {
                        Text(stringResource(R.string.view_on_map), fontWeight = FontWeight.Bold)
                        Text("  →", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

