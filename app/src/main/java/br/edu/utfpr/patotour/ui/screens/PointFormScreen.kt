package br.edu.utfpr.patotour.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.ui.focus.onFocusChanged
import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import br.edu.utfpr.patotour.R
import br.edu.utfpr.patotour.data.model.PontoTuristico
import br.edu.utfpr.patotour.service.GeocodingService
import br.edu.utfpr.patotour.ui.components.TopBar
import br.edu.utfpr.patotour.ui.components.LocationPickerDialog
import br.edu.utfpr.patotour.ui.components.TouristPointImage
import br.edu.utfpr.patotour.util.createCameraUri
import br.edu.utfpr.patotour.util.salvarImagemLocal
import br.edu.utfpr.patotour.util.toCleanDoubleOrNull
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun PointFormScreen(
    initial: PontoTuristico?,
    onBack: () -> Unit,
    onSave: (PontoTuristico) -> Unit,
    onDelete: (PontoTuristico) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val geocodingService = remember(context) { GeocodingService(context.applicationContext) }

    // A rotação recria a Activity; estes campos precisam sobreviver a esse ciclo.
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.nome ?: "") }
    var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.descricao ?: "") }
    var latitude by rememberSaveable(initial?.id) { mutableStateOf(initial?.latitude?.toString() ?: "") }
    var longitude by rememberSaveable(initial?.id) { mutableStateOf(initial?.longitude?.toString() ?: "") }
    var address by rememberSaveable(initial?.id) { mutableStateOf(initial?.enderecoTextual ?: "") }
    var imageUri by rememberSaveable(initial?.id) { mutableStateOf(initial?.caminhoImagem ?: "") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var addressLoading by rememberSaveable { mutableStateOf(false) }
    var locationPickerOpen by rememberSaveable { mutableStateOf(false) }
    var deleteConfirmOpen by rememberSaveable { mutableStateOf(false) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    var latTouched by remember { mutableStateOf(false) }
    var lonTouched by remember { mutableStateOf(false) }

    val typingStates = listOf("-", ".", ",", "-.", "-,")

    val latDouble = latitude.toCleanDoubleOrNull()
    val isLatValid = when {
        latTouched -> latDouble != null && latDouble in -90.0..90.0
        else -> latitude.isBlank() || latitude.trim() in typingStates || (latDouble?.let { it in -90.0..90.0 } ?: false)
    }

    val lonDouble = longitude.toCleanDoubleOrNull()
    val isLonValid = when {
        lonTouched -> lonDouble != null && lonDouble in -180.0..180.0
        else -> longitude.isBlank() || longitude.trim() in typingStates || (lonDouble?.let { it in -180.0..180.0 } ?: false)
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) scope.launch { imageUri = salvarImagemLocal(context, uri) ?: uri.toString() }
    }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        if (captured) cameraUri?.let { uri -> scope.launch { imageUri = salvarImagemLocal(context, uri) ?: uri.toString() } }
    }
    val requestCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cameraUri = createCameraUri(context)
            cameraUri?.let(camera::launch)
        }
    }
    fun requestAddress(lat: Double?, lon: Double?) {
        if (lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
            message = context.getString(R.string.coordinates_required)
            return
        }
        scope.launch {
            addressLoading = true
            message = null
            address = geocodingService.buscarEnderecoPorCoordenadas(lat, lon)
                ?: context.getString(R.string.address_unavailable)
            addressLoading = false
        }
    }

    @SuppressLint("MissingPermission")
    fun useCurrentLocationForAddress() {
        if (!hasLocationPermission(context)) return
        addressLoading = true
        message = null
        fun applyLocation(location: android.location.Location?) {
            if (location == null) {
                addressLoading = false
                message = context.getString(R.string.location_unavailable)
                return
            }
            latitude = String.format(Locale.US, "%.6f", location.latitude)
            longitude = String.format(Locale.US, "%.6f", location.longitude)
            requestAddress(location.latitude, location.longitude)
        }
        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (location != null) applyLocation(location)
                else fusedLocationClient.lastLocation.addOnSuccessListener(::applyLocation)
            }
            .addOnFailureListener { applyLocation(null) }
    }

    val requestLocation = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) useCurrentLocationForAddress()
        else message = context.getString(R.string.location_permission_required)
    }

    Scaffold(
        topBar = {
            TopBar(
                if (initial == null) stringResource(R.string.new_point) else stringResource(R.string.edit_point),
                onBack
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        if (initial != null) {
                            TextButton(
                                onClick = { deleteConfirmOpen = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) { Text(stringResource(R.string.delete)) }
                        }
                        TextButton(onClick = onBack) { Text(stringResource(R.string.cancel)) }
                    }
                    Button(onClick = {
                        latTouched = true
                        lonTouched = true

                        val lat = latitude.toCleanDoubleOrNull()
                        val lon = longitude.toCleanDoubleOrNull()

                        if (name.isBlank() || lat == null || lon == null || !isLatValid || !isLonValid) {
                            message = context.getString(R.string.required_fields)
                        } else {
                            onSave(
                                PontoTuristico(
                                    initial?.id ?: 0,
                                    name.trim(),
                                    description.trim(),
                                    lat,
                                    lon,
                                    address,
                                    imageUri
                                )
                            )
                        }
                    }) { Text(stringResource(R.string.save_offline)) }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                if (initial == null) stringResource(R.string.new_point_title) else stringResource(R.string.edit_point_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(stringResource(R.string.form_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PhotoPicker(imageUri) { picker.launch("image/*") }
                    OutlinedButton(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraUri = createCameraUri(context)
                            cameraUri?.let(camera::launch)
                        } else {
                            requestCamera.launch(Manifest.permission.CAMERA)
                        }
                    }, Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text(stringResource(R.string.take_photo))
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(stringResource(R.string.place_details), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FormField(stringResource(R.string.point_name), name, { name = it }, stringResource(R.string.point_name_hint))
                    FormField(stringResource(R.string.description), description, { description = it }, stringResource(R.string.description_hint), singleLine = false)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.location_coordinates),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FormField(
                            label = stringResource(R.string.latitude),
                            value = latitude,
                            onValueChange = { latitude = it },
                            hint = stringResource(R.string.latitude_hint),
                            modifier = Modifier.weight(1f),
                            number = true,
                            isError = !isLatValid,
                            errorMessage = if (!isLatValid) "Inválido (-90 a 90)" else null,
                            onFocusChanged = { isFocused ->
                                if (!isFocused && latitude.isNotEmpty()) {
                                    latTouched = true
                                }
                            }
                        )
                        FormField(
                            label = stringResource(R.string.longitude),
                            value = longitude,
                            onValueChange = { longitude = it },
                            hint = stringResource(R.string.longitude_hint),
                            modifier = Modifier.weight(1f),
                            number = true,
                            isError = !isLonValid,
                            errorMessage = if (!isLonValid) "Inválido (-180 a 180)" else null,
                            onFocusChanged = { isFocused ->
                                if (!isFocused && longitude.isNotEmpty()) {
                                    lonTouched = true
                                }
                            }
                        )
                    }
                    if (isLandscape) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FormField(stringResource(R.string.latitude), latitude, { latitude = it }, stringResource(R.string.latitude_hint), Modifier.weight(1f), true)
                            FormField(stringResource(R.string.longitude), longitude, { longitude = it }, stringResource(R.string.longitude_hint), Modifier.weight(1f), true)
                        }
                    } else {
                        FormField(stringResource(R.string.latitude), latitude, { latitude = it }, stringResource(R.string.latitude_hint), number = true)
                        FormField(stringResource(R.string.longitude), longitude, { longitude = it }, stringResource(R.string.longitude_hint), number = true)
                    }
                    Button(onClick = { locationPickerOpen = true }, Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.MyLocation, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text(stringResource(R.string.select_on_map))
                    }
                    OutlinedButton(onClick = {
                        latTouched = true
                        lonTouched = true
                        requestAddress(latitude.toCleanDoubleOrNull(), longitude.toCleanDoubleOrNull())
                    }, Modifier.fillMaxWidth(), enabled = !addressLoading) {
                        if (addressLoading) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp)
                        else Text(stringResource(R.string.get_address))
                    }
                    Text(
                        stringResource(R.string.address_current_location_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FormField(
                        stringResource(R.string.address),
                        address,
                        { address = it },
                        stringResource(R.string.address_hint),
                        singleLine = false
                    )
                }
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }

    if (locationPickerOpen) {
        LocationPickerDialog(
            initialLatitude = latitude.toCleanDoubleOrNull(),
            initialLongitude = longitude.toCleanDoubleOrNull(),
            onDismiss = { locationPickerOpen = false },
            onConfirm = { selectedLatitude, selectedLongitude ->
                latitude = String.format(Locale.US, "%.6f", selectedLatitude)
                longitude = String.format(Locale.US, "%.6f", selectedLongitude)
                latTouched = true
                lonTouched = true
                locationPickerOpen = false
                requestAddress(selectedLatitude, selectedLongitude)
            }
        )
    }

    if (deleteConfirmOpen && initial != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmOpen = false },
            title = { Text(stringResource(R.string.delete_point_title)) },
            text = { Text(stringResource(R.string.delete_point_message, initial.nome)) },
            confirmButton = {
                TextButton(onClick = {
                    deleteConfirmOpen = false
                    onDelete(initial)
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmOpen = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    number: Boolean = false,
    singleLine: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null
) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    onFocusChanged?.invoke(focusState.isFocused)
                },
            placeholder = { Text(hint) },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            isError = isError,
            supportingText = {
                if (isError && errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Decimal else KeyboardType.Text),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun PhotoPicker(uri: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (uri.isBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(42.dp))
                Text(stringResource(R.string.upload_photo), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.photo_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        } else {
            TouristPointImage(uri, Modifier.fillMaxSize(), stringResource(R.string.point_photo))
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
