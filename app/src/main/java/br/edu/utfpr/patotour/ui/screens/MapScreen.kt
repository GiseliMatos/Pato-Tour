 package br.edu.utfpr.patotour.ui.screens
 
 import android.Manifest
import android.content.Context
 import android.content.pm.PackageManager
 import androidx.activity.compose.rememberLauncherForActivityResult
 import androidx.activity.result.contract.ActivityResultContracts
 import androidx.compose.foundation.layout.*
 import androidx.compose.foundation.lazy.LazyColumn
 import androidx.compose.foundation.lazy.items
 import androidx.compose.material3.*
 import androidx.compose.runtime.*
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.platform.LocalContext
 import androidx.compose.ui.res.stringResource
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.unit.dp
 import androidx.core.content.ContextCompat
 import br.edu.utfpr.patotour.R
import br.edu.utfpr.patotour.data.model.PontoTuristico
import br.edu.utfpr.patotour.preferences.ConfiguracoesPreferences
 import br.edu.utfpr.patotour.ui.components.BottomBar
 import br.edu.utfpr.patotour.ui.components.MainDestination
 import br.edu.utfpr.patotour.ui.components.TopBar
 import br.edu.utfpr.patotour.ui.components.TouristPointImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
 import com.google.android.gms.maps.CameraUpdateFactory
 import com.google.android.gms.maps.model.CameraPosition
 import com.google.android.gms.maps.model.LatLng
 import com.google.maps.android.compose.GoogleMap
 import com.google.maps.android.compose.MapProperties
 import com.google.maps.android.compose.MapType
 import com.google.maps.android.compose.MapUiSettings
 import com.google.maps.android.compose.Marker
 import com.google.maps.android.compose.MarkerState
 import com.google.maps.android.compose.rememberCameraPositionState
 import kotlinx.coroutines.launch
 
 @Composable
fun MapScreen(points: List<PontoTuristico>, focusPoint: PontoTuristico?, onBack: () -> Unit, onNavigate: (MainDestination) -> Unit) {
     val context = LocalContext.current
    val preferences = remember { ConfiguracoesPreferences(context) }
     val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
     val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(focusPoint) }
    var hasLocationPermission by remember {
        mutableStateOf(
            hasAnyLocationPermission(context)
        )
     }
     val requestLocation = rememberLauncherForActivityResult(
         ActivityResultContracts.RequestMultiplePermissions()
     ) {
        hasLocationPermission = hasAnyLocationPermission(context)
     }
 
     val zoom = preferences.obterZoom()
     val mapType = preferences.obterTipoMapa()
    val initialTarget = focusPoint?.let { LatLng(it.latitude, it.longitude) } ?: points.firstOrNull()?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(-23.5505, -46.6333)
     val cameraPositionState = rememberCameraPositionState {
         position = CameraPosition.fromLatLngZoom(initialTarget, zoom)
     }
 
     fun centerOnCurrentLocation() {
         if (!hasLocationPermission) {
             requestLocation.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
             return
         }
        fun moveToLocation(location: android.location.Location?) {
            if (location != null) {
                scope.launch {
                     cameraPositionState.animate(
                         CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), zoom),
                         700
                     )
                }
            }
        }
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) moveToLocation(location)
            else fusedLocationClient.lastLocation.addOnSuccessListener(::moveToLocation)
        }
     }
 
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) centerOnCurrentLocation()
    }
    LaunchedEffect(focusPoint) {
        focusPoint?.let { point ->
            selected = point
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(point.latitude, point.longitude), zoom), 500)
        }
    }
 
     Scaffold(
         topBar = { TopBar(stringResource(R.string.map), onBack) },
         bottomBar = { BottomBar(MainDestination.MAP, onNavigate) }
     ) { padding ->
         Column(
             Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp),
             verticalArrangement = Arrangement.spacedBy(10.dp)
         ) {
             Text(stringResource(R.string.map_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
             Text(stringResource(R.string.map_type_value, stringResource(mapTypeString(mapType))), color = MaterialTheme.colorScheme.onSurfaceVariant)
             GoogleMap(
                 modifier = Modifier.fillMaxWidth().height(320.dp),
                 cameraPositionState = cameraPositionState,
                 properties = MapProperties(mapType = googleMapType(mapType), isMyLocationEnabled = hasLocationPermission),
                 uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = false, myLocationButtonEnabled = hasLocationPermission)
             ) {
                 points.forEach { point ->
                     Marker(
                         state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                         title = point.nome,
                         snippet = point.enderecoTextual.ifBlank { point.latitude.toString() + ", " + point.longitude.toString() },
                         onClick = { selected = point; false }
                     )
                 }
             }
             OutlinedButton(onClick = { centerOnCurrentLocation() }, Modifier.fillMaxWidth()) {
                 Text(stringResource(if (hasLocationPermission) R.string.use_current_location else R.string.enable_location))
             }
             if (points.isEmpty()) {
                 Text(stringResource(R.string.empty_map), color = MaterialTheme.colorScheme.onSurfaceVariant)
             } else {
                 Text(stringResource(R.string.map_points_list), fontWeight = FontWeight.Bold)
                 LazyColumn(
                     Modifier.fillMaxWidth().weight(1f),
                     verticalArrangement = Arrangement.spacedBy(8.dp),
                     contentPadding = PaddingValues(bottom = 8.dp)
                 ) {
                     items(points, key = { it.id }) { point ->
                         MapPointCard(point, point.id == selected?.id) {
                             selected = point
                             scope.launch {
                                 cameraPositionState.animate(
                                     CameraUpdateFactory.newLatLngZoom(LatLng(point.latitude, point.longitude), zoom),
                                     500
                                 )
                             }
                         }
                     }
                 }
             }
             selected?.let { point ->
                 Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                     Row(Modifier.padding(10.dp)) {
                         TouristPointImage(point.caminhoImagem, Modifier.size(76.dp), point.nome)
                         Column(Modifier.padding(start = 12.dp)) {
                             Text(point.nome, fontWeight = FontWeight.Bold)
                             Text(point.descricao, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                             Text(point.enderecoTextual.ifBlank { point.latitude.toString() + ", " + point.longitude.toString() }, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                         }
                     }
                 }
             }
         }
     }
 }
 
 @Composable
 private fun MapPointCard(point: PontoTuristico, selected: Boolean, onClick: () -> Unit) {
     Card(
         onClick = onClick,
         modifier = Modifier.fillMaxWidth(),
         colors = CardDefaults.cardColors(
             containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest
         )
     ) {
         Row(Modifier.padding(10.dp)) {
             TouristPointImage(point.caminhoImagem, Modifier.size(64.dp), point.nome)
             Column(Modifier.padding(start = 12.dp)) {
                 Text(point.nome, fontWeight = FontWeight.Bold)
                 Text(point.enderecoTextual.ifBlank { point.latitude.toString() + ", " + point.longitude.toString() }, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
             }
         }
     }
 }
 
 private fun mapTypeString(type: String): Int = when (type) {
     "Satélite" -> R.string.map_satellite
     "Híbrido" -> R.string.map_hybrid
     "Terreno" -> R.string.map_terrain
     else -> R.string.map_roadmap
 }
 
private fun googleMapType(type: String): MapType = when (type) {
     "Satélite" -> MapType.SATELLITE
     "Híbrido" -> MapType.HYBRID
     "Terreno" -> MapType.TERRAIN
     else -> MapType.NORMAL
}

private fun hasAnyLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
 
