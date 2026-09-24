package br.edu.utfpr.patotour.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import br.edu.utfpr.patotour.R
import br.edu.utfpr.patotour.util.loadOrientedBitmap

@Composable
fun TouristPointImage(uri: String, modifier: Modifier, contentDescription: String? = null) {
    val context = LocalContext.current
    val bitmap = remember(uri) { if (uri.isBlank()) null else loadOrientedBitmap(context, uri) }
    if (bitmap != null) {
        Image(bitmap.asImageBitmap(), contentDescription, modifier, contentScale = ContentScale.Crop)
    } else {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_photo), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
