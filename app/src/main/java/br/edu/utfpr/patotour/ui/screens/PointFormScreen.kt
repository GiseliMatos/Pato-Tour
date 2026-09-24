package br.edu.utfpr.patotour.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import br.edu.utfpr.patotour.R
import br.edu.utfpr.patotour.data.model.PontoTuristico
import br.edu.utfpr.patotour.service.GeocodingService
import br.edu.utfpr.patotour.ui.components.TopBar
import br.edu.utfpr.patotour.ui.components.LocationPickerDialog
import br.edu.utfpr.patotour.ui.components.TouristPointImage
import br.edu.utfpr.patotour.util.createCameraUri
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun PointFormScreen(
    initial: PontoTuristico?,
    onBack: () -> Unit,
    onSave: (PontoTuristico) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val geocodingService = remember { GeocodingService(context) }

    var name by remember { mutableStateOf(initial?.nome ?: "") }
    var description by remember { mutableStateOf(initial?.descricao ?: "") }
    var latitude by remember { mutableStateOf(initial?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(initial?.longitude?.toString() ?: "") }
    var address by remember { mutableStateOf(initial?.enderecoTextual ?: "") }
    var imageUri by remember { mutableStateOf(initial?.caminhoImagem ?: "") }
    var message by remember { mutableStateOf<String?>(null) }
    var addressLoading by remember { mutableStateOf(false) }
    var locationPickerOpen by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) imageUri = uri.toString()
    }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        if (captured) imageUri = cameraUri?.toString().orEmpty()
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
                    TextButton(onClick = onBack) { Text(stringResource(R.string.cancel)) }
                    Button(onClick = {
                        val lat = latitude.toDoubleOrNull()
                        val lon = longitude.toDoubleOrNull()
                        if (name.isBlank() || lat == null || lon == null) {
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
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (initial == null) stringResource(R.string.new_point_title) else stringResource(R.string.edit_point_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(stringResource(R.string.form_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)

            PhotoPicker(imageUri) { picker.launch("image/*") }
            OutlinedButton(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraUri = createCameraUri(context)
                    cameraUri?.let(camera::launch)
                } else {
                    requestCamera.launch(Manifest.permission.CAMERA)
                }
            }, Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.take_photo))
            }

            FormField(stringResource(R.string.point_name), name, { name = it }, stringResource(R.string.point_name_hint))
            FormField(stringResource(R.string.description), description, { description = it }, stringResource(R.string.description_hint), singleLine = false)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.location_coordinates),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FormField(stringResource(R.string.latitude), latitude, { latitude = it }, stringResource(R.string.latitude_hint), Modifier.weight(1f), true)
                        FormField(stringResource(R.string.longitude), longitude, { longitude = it }, stringResource(R.string.longitude_hint), Modifier.weight(1f), true)
                    }
                    OutlinedButton(onClick = { locationPickerOpen = true }, Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.select_on_map))
                    }
                    OutlinedButton(onClick = {
                        requestAddress(latitude.toDoubleOrNull(), longitude.toDoubleOrNull())
                    }, Modifier.fillMaxWidth(), enabled = !addressLoading) {
                        if (addressLoading) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp)
                        else Text(stringResource(R.string.get_address))
                    }
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
            initialLatitude = latitude.toDoubleOrNull(),
            initialLongitude = longitude.toDoubleOrNull(),
            onDismiss = { locationPickerOpen = false },
            onConfirm = { selectedLatitude, selectedLongitude ->
                latitude = String.format(Locale.US, "%.6f", selectedLatitude)
                longitude = String.format(Locale.US, "%.6f", selectedLongitude)
                locationPickerOpen = false
                requestAddress(selectedLatitude, selectedLongitude)
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
    singleLine: Boolean = true
) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value,
            onValueChange,
            Modifier.fillMaxWidth(),
            placeholder = { Text(hint) },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Decimal else KeyboardType.Text),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
private fun PhotoPicker(uri: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (uri.isBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("+", fontSize = 44.sp, color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.upload_photo), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.photo_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        } else {
            TouristPointImage(uri, Modifier.fillMaxSize(), stringResource(R.string.point_photo))
        }
    }
}

