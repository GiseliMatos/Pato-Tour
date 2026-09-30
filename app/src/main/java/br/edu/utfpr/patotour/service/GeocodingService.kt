package br.edu.utfpr.patotour.service

import br.edu.utfpr.patotour.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeocodingService {
    suspend fun buscarEnderecoPorCoordenadas(
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        if (BuildConfig.MAPS_API_KEY == "DEFAULT_API_KEY") return@withContext null

        runCatching {
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
}
