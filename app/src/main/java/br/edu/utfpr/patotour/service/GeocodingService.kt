package br.edu.utfpr.patotour.service

import android.content.Context
import android.location.Address
import android.location.Geocoder
import br.edu.utfpr.patotour.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class GeocodingService(private val context: Context) {
    suspend fun buscarEnderecoPorCoordenadas(
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        buscarComGeocoderDoSistema(latitude, longitude)
            ?: buscarComApiDoGoogle(latitude, longitude)
    }

    @Suppress("DEPRECATION")
    private fun buscarComGeocoderDoSistema(latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) return null

        return runCatching {
            Geocoder(context, Locale("pt", "BR"))
                .getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()
                ?.formatarEndereco()
        }.getOrNull()
    }

    private fun buscarComApiDoGoogle(latitude: Double, longitude: Double): String? {
        if (BuildConfig.MAPS_API_KEY == "DEFAULT_API_KEY") return null

        return runCatching {
            val endpoint = "https://maps.googleapis.com/maps/api/geocode/json" +
                "?latlng=$latitude,$longitude&language=pt-BR&key=${BuildConfig.MAPS_API_KEY}"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            try {
                if (connection.responseCode !in 200..299) return@runCatching null
                val response = connection.inputStream.bufferedReader().use { reader -> reader.readText() }
                val json = JSONObject(response)
                if (json.optString("status") != "OK") return@runCatching null
                json.optJSONArray("results")
                    ?.optJSONObject(0)
                    ?.optString("formatted_address")
                    ?.takeIf { address -> address.isNotBlank() }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    private fun Address.formatarEndereco(): String? =
        (0..maxAddressLineIndex)
            .mapNotNull(::getAddressLine)
            .joinToString(", ")
            .takeIf(String::isNotBlank)
}
