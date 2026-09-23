package br.edu.utfpr.patotour.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.patotour.R
import br.edu.utfpr.patotour.data.model.PontoTuristico

@Composable
fun PointCard(point: PontoTuristico, onEdit: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.heightIn(min = 140.dp)) {
            CardImage(point.caminhoImagem, Modifier.width(120.dp).fillMaxHeight())
            Column(Modifier.padding(16.dp).weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(point.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "⌖ ${point.latitude}, ${point.longitude}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    if (point.enderecoTextual.isNotBlank()) {
                        Text(
                            point.enderecoTextual,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
                Text(
                    stringResource(R.string.edit_point),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun CardImage(uri: String, modifier: Modifier) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        if (uri.isBlank()) {
            null
        } else {
            runCatching {
                android.graphics.BitmapFactory.decodeStream(context.contentResolver.openInputStream(Uri.parse(uri)))
            }.getOrNull()
        }
    }
    if (bitmap != null) {
        Image(bitmap.asImageBitmap(), null, modifier, contentScale = ContentScale.Crop)
    } else {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
            Text("⌖", fontSize = 34.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}