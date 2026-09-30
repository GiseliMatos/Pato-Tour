package br.edu.utfpr.patotour.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import br.edu.utfpr.patotour.R
import br.edu.utfpr.patotour.data.model.PontoTuristico
import br.edu.utfpr.patotour.preferences.ConfiguracoesPreferences
import br.edu.utfpr.patotour.ui.components.BottomBar
import br.edu.utfpr.patotour.ui.components.MainDestination
import br.edu.utfpr.patotour.ui.components.TouristPointImage
import androidx.compose.ui.window.Dialog
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    points: List<PontoTuristico>,
    focusPoint: PontoTuristico?,
    onNavigate: (MainDestination) -> Unit,
    onAddPoint: () -> Unit,
    onOpenDetails: (PontoTuristico) -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { ConfiguracoesPreferences(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(focusPoint) }
    var detalhesAbertos by remember { mutableStateOf<PontoTuristico?>(null) }
    var hasLocationPermission by remember { mutableStateOf(hasAnyLocationPermission(context)) }
    val requestLocation = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasLocationPermission = hasAnyLocationPermission(context)
    }

    val zoom = preferences.obterZoom()
    val mapType = preferences.obterTipoMapa()
    val sugestoes = if (query.isBlank()) emptyList() else points.filter {
        it.nome.contains(query, true) || it.enderecoTextual.contains(query, true)
    }

    val initialTarget = focusPoint?.let { LatLng(it.latitude, it.longitude) }
        ?: points.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
        ?: LatLng(-23.5505, -46.6333)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialTarget, zoom)
    }

    fun moverPara(ponto: PontoTuristico) {
        selected = ponto
        scope.launch {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(ponto.latitude, ponto.longitude), zoom), 500)
        }
    }

    fun centerOnCurrentLocation() {
        if (!hasAnyLocationPermission(context)) {
            requestLocation.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }
        fun moverParaLocalizacao(location: android.location.Location?) {
            val alvo = location ?: return
            scope.launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(alvo.latitude, alvo.longitude), zoom), 700)
            }
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (location != null) moverParaLocalizacao(location)
                else fusedLocationClient.lastLocation.addOnSuccessListener(::moverParaLocalizacao)
            }
    }

    // Se veio de "Ver no mapa", vai até o ponto. Sem foco definido, centraliza na localização atual.
    LaunchedEffect(focusPoint, hasLocationPermission) {
        if (focusPoint != null) {
            moverPara(focusPoint)
        } else {
            centerOnCurrentLocation()
        }
    }

    Scaffold(bottomBar = { BottomBar(MainDestination.MAP, onNavigate) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = googleMapType(mapType), isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false)
            ) {
                points.forEach { ponto ->
                    val destacado = ponto.id == selected?.id
                    Marker(
                        state = rememberMarkerState(
                            key = "${ponto.id}:${ponto.latitude}:${ponto.longitude}",
                            position = LatLng(ponto.latitude, ponto.longitude)
                        ),
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (destacado) BitmapDescriptorFactory.HUE_ORANGE else BitmapDescriptorFactory.HUE_AZURE
                        ),
                        onClick = { selected = ponto; false }
                    )
                }
            }

            // Barra de busca
            Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Text("⌕", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text(stringResource(R.string.search_points)) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
                if (sugestoes.isNotEmpty()) {
                    Card(
                        Modifier.fillMaxWidth().padding(top = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column {
                            sugestoes.take(5).forEach { ponto ->
                                Text(
                                    ponto.nome,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            moverPara(ponto)
                                            query = ""
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Controles de zoom
            Column(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 88.dp, end = 16.dp)
                    .width(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                ZoomButton("+") { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) } }
                HorizontalDivider(Modifier.width(44.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ZoomButton("−") { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) } }
            }

            // Voltar para a localização atual (sobrepõe o focusPoint quando tocado)
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 189.dp, end = 16.dp)
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .clickable { centerOnCurrentLocation() },
                contentAlignment = Alignment.Center
            ) {
                Text("⌖", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }

            // Botão de adicionar ponto
            FloatingActionButton(
                onClick = onAddPoint,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = if (selected != null) 148.dp else 16.dp),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ) { Text("+", fontSize = 28.sp) }

            // Card inferior do ponto selecionado
            selected?.let { ponto ->
                Card(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.padding(12.dp)) {
                        TouristPointImage(ponto.caminhoImagem, Modifier.size(84.dp).clip(RoundedCornerShape(12.dp)), ponto.nome)
                        Column(Modifier.padding(start = 12.dp).weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(ponto.nome, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { selected = null })
                            }
                            Text(
                                ponto.descricao.ifBlank { ponto.enderecoTextual },
                                fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    ponto.enderecoTextual.ifBlank { "${ponto.latitude}, ${ponto.longitude}" },
                                    fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    stringResource(R.string.view_details),
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { detalhesAbertos = ponto }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    detalhesAbertos?.let { ponto ->
        Dialog(onDismissRequest = { detalhesAbertos = null }) {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TouristPointImage(ponto.caminhoImagem, Modifier.fillMaxWidth().height(180.dp), ponto.nome)
                    Column(Modifier.padding(20.dp)) {
                        Text(ponto.nome, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (ponto.descricao.isNotBlank()) {
                            Text(
                                ponto.descricao,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Text(
                            ponto.enderecoTextual.ifBlank { stringResource(R.string.address_unavailable) },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            "${ponto.latitude}, ${ponto.longitude}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            Modifier.fillMaxWidth().padding(top = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { detalhesAbertos = null }) {
                                Text(stringResource(R.string.close))
                            }
                            Button(onClick = { detalhesAbertos = null; onOpenDetails(ponto) }) {
                                Text(stringResource(R.string.edit_point))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
    }
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