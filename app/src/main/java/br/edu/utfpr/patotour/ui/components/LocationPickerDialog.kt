package br.edu.utfpr.patotour.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import br.edu.utfpr.patotour.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun LocationPickerDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val initialPosition = LatLng(initialLatitude ?: -23.5505, initialLongitude ?: -46.6333)
    var selectedPosition by remember { mutableStateOf(initialPosition) }
    val scope = rememberCoroutineScope()
    var hasLocationPermission by remember { mutableStateOf(hasAnyLocationPermission(context)) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val requestLocation = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasLocationPermission = hasAnyLocationPermission(context)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 14f)
    }

    fun useCurrentLocation() {
        if (!hasLocationPermission) {
            requestLocation.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                val current = LatLng(location.latitude, location.longitude)
                selectedPosition = current
                scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(current, 16f), 500) }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.select_location_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.select_location_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { useCurrentLocation() }, Modifier.fillMaxWidth()) {
                    Text(stringResource(if (hasLocationPermission) R.string.use_current_location else R.string.enable_location))
                }
                GoogleMap(
                    modifier = Modifier.fillMaxWidth().height(420.dp),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(mapType = MapType.NORMAL),
                    onMapClick = { selectedPosition = it }
                ) {
                    Marker(
                        state = MarkerState(position = selectedPosition),
                        title = stringResource(R.string.selected_location)
                    )
                }
                Text(
                    stringResource(R.string.coordinates_selected, selectedPosition.latitude, selectedPosition.longitude),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    Button(onClick = { onConfirm(selectedPosition.latitude, selectedPosition.longitude) }, modifier = Modifier.padding(start = 8.dp)) {
                        Text(stringResource(R.string.confirm_location))
                    }
                }
            }
        }
    }
}

private fun hasAnyLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
