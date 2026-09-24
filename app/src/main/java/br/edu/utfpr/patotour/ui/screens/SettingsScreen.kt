 package br.edu.utfpr.patotour.ui.screens
 
 import android.Manifest
 import android.content.Context
 import android.content.pm.PackageManager
 import androidx.activity.compose.rememberLauncherForActivityResult
 import androidx.activity.result.contract.ActivityResultContracts
 import androidx.compose.foundation.clickable
 import androidx.compose.foundation.layout.*
 import androidx.compose.material3.*
 import androidx.compose.runtime.*
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.platform.LocalContext
 import androidx.compose.ui.res.stringResource
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.unit.dp
 import br.edu.utfpr.patotour.R
 import br.edu.utfpr.patotour.ui.components.BottomBar
 import br.edu.utfpr.patotour.ui.components.MainDestination
 import br.edu.utfpr.patotour.ui.components.TopBar
 import androidx.core.content.ContextCompat
 
 @Composable
 fun SettingsScreen(onBack: () -> Unit, onNavigate: (MainDestination) -> Unit) {
     val context = LocalContext.current
     val preferences = remember { context.getSharedPreferences("pato_tour_preferences", Context.MODE_PRIVATE) }
     var zoom by remember { mutableFloatStateOf(preferences.getFloat("zoom", 12f)) }
     var mapType by remember { mutableStateOf(preferences.getString("map_type", "roadmap") ?: "roadmap") }
     var cameraGranted by remember { mutableStateOf(hasPermission(context, Manifest.permission.CAMERA)) }
     var locationGranted by remember { mutableStateOf(hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) }
     val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
         cameraGranted = hasPermission(context, Manifest.permission.CAMERA)
         locationGranted = hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
     }
 
     Scaffold(
         topBar = { TopBar(stringResource(R.string.settings), onBack) },
         bottomBar = { BottomBar(MainDestination.SETTINGS, onNavigate) }
     ) { padding ->
         Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
             Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
             Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                 Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                     Text(stringResource(R.string.default_zoom), fontWeight = FontWeight.Bold)
                     Text(stringResource(R.string.map_zoom_value, zoom.toInt()), color = MaterialTheme.colorScheme.onSurfaceVariant)
                     Slider(zoom, { zoom = it; preferences.edit().putFloat("zoom", it).apply() }, valueRange = 5f..20f, steps = 14)
                     Text(stringResource(R.string.map_type), fontWeight = FontWeight.Bold)
                     listOf("roadmap" to R.string.roadmap, "satellite" to R.string.satellite, "hybrid" to R.string.hybrid).forEach { (value, label) ->
                         Row(Modifier.fillMaxWidth().clickable { mapType = value; preferences.edit().putString("map_type", value).apply() }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                             RadioButton(mapType == value, { mapType = value; preferences.edit().putString("map_type", value).apply() })
                             Text(stringResource(label))
                         }
                     }
                 }
             }
             Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                 Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                     Text(stringResource(R.string.permissions), fontWeight = FontWeight.Bold)
                     Text(stringResource(R.string.permission_status, if (cameraGranted) stringResource(R.string.permission_allowed) else stringResource(R.string.permission_pending), if (locationGranted) stringResource(R.string.permission_allowed) else stringResource(R.string.permission_pending)), color = MaterialTheme.colorScheme.onSurfaceVariant)
                     OutlinedButton(onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)) }, Modifier.fillMaxWidth()) {
                         Text(stringResource(R.string.manage_permissions))
                     }
                 }
             }
         }
     }
 }
 
 private fun hasPermission(context: Context, permission: String): Boolean =
     ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
 
