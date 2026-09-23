package br.edu.utfpr.patotour.service

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class GeocodingService(private val context: Context) {
    suspend fun buscarEnderecoPorCoordenadas(
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null

        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                val enderecoFormatado = addresses.firstOrNull()?.let { formatarEndereco(it) }
                                continuation.resume(enderecoFormatado)
                            }

                            override fun onError(errorMessage: String?) {
                                continuation.resume(null)
                            }
                        })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.let { formatarEndereco(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun formatarEndereco(address: Address): String {
        val rua = address.thoroughfare
        val numero = address.subThoroughfare
        val bairro = address.subLocality
        val cidade = address.locality ?: address.subAdminArea

        val partes = mutableListOf<String>()
        if (!rua.isNullOrBlank()) {
            val logradouro = if (!numero.isNullOrBlank()) "$rua, $numero" else rua
            partes.add(logradouro)
        }
        if (!bairro.isNullOrBlank()) partes.add(bairro)
        if (!cidade.isNullOrBlank()) partes.add(cidade)

        return if (partes.isNotEmpty())
            partes.joinToString(" - ")
        else
            address.getAddressLine(0) ?: ""
    }
}