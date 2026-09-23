package br.edu.utfpr.patotour.util

import android.content.ContentValues
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            if (!Geocoder.isPresent()) return@withContext null
            Geocoder(context).getFromLocation(latitude, longitude, 1)?.firstOrNull()?.getAddressLine(0)
        }.getOrNull()
    }

fun createCameraUri(context: Context): Uri? = runCatching {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "pato_tour_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PatoTour")
    }
    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
}.getOrNull()