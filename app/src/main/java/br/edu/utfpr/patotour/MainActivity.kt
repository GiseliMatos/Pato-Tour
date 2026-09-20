package br.edu.utfpr.patotour

import android.content.Context
import android.content.ContentValues
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.patotour.database.local.AppDatabase
import br.edu.utfpr.patotour.model.PontoTuristico
import br.edu.utfpr.patotour.repository.PontoTuristicoRepository
import br.edu.utfpr.patotour.ui.theme.PatoTourTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import br.edu.utfpr.patotour.ui.screens.ConfiguracoesScreen

private val ExplorerBlue = Color(0xFF004D99)
private val ExplorerGreen = Color(0xFF1B6D24)
private val ExplorerBackground = Color(0xFFF3FAFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PatoTourTheme(dynamicColor = false) { PatoTourApp() } }
    }
}

private enum class Screen { LIST, FORM, MAP, SETTINGS }

@Composable
private fun PatoTourApp() {
    val context = LocalContext.current
    val repository = remember { PontoTuristicoRepository(AppDatabase.getInstance(context).pontoTuristicoDao()) }
    val points by repository.todosPontos.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(Screen.LIST) }
    var editing by remember { mutableStateOf<PontoTuristico?>(null) }
    Surface(color = ExplorerBackground, modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (screen != Screen.FORM) {
                    BottomBar(selectedScreen = screen, onNavigate = { novaTela -> screen = novaTela }) }
            }
        ) { padding -> Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                Screen.LIST -> PointsScreen(points, { editing = null; screen = Screen.FORM }) { editing = it; screen = Screen.FORM }
                Screen.FORM -> PointFormScreen(editing, { screen = Screen.LIST }) { point ->
                    scope.launch { repository.salvar(point); screen = Screen.LIST }
                }
                Screen.MAP -> { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("") } }
                Screen.SETTINGS -> ConfiguracoesScreen()
            }
        }
        }
    }
}

@Composable
private fun ExplorerTopBar(title: String, back: (() -> Unit)? = null) {
    Surface(shadowElevation = 2.dp, color = Color.White) {
        Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (back != null) Text("‹", fontSize = 40.sp, color = ExplorerBlue, modifier = Modifier.size(40.dp).clickable { back() })
            else Spacer(Modifier.width(40.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = ExplorerBlue, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.width(40.dp))
        }
    }
}

@Composable
private fun PointsScreen(points: List<PontoTuristico>, onAdd: () -> Unit, onEdit: (PontoTuristico) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = points.filter { it.nome.contains(query, true) || it.enderecoTextual.contains(query, true) }
    Scaffold(topBar = { ExplorerTopBar(stringResource(R.string.app_name)) }, floatingActionButton = { FloatingActionButton(onClick = onAdd, containerColor = Color(0xFF7D3C00), contentColor = Color.White) { Text("+", fontSize = 28.sp) } }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)) {
            item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.search_points)) }, singleLine = true, shape = RoundedCornerShape(14.dp)) }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) { Text(stringResource(R.string.registered_points), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(stringResource(R.string.points_count, filtered.size), color = Color.Gray) } }
            if (filtered.isEmpty()) item { EmptyState(onAdd) } else items(filtered, key = { it.id }) { point -> PointCard(point) { onEdit(point) } }
        }
    }
}

@Composable
private fun PointCard(point: PontoTuristico, onEdit: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onEdit() }, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.heightIn(min = 140.dp)) {
            PointImage(point.caminhoImagem, Modifier.width(120.dp).fillMaxHeight())
            Column(Modifier.padding(16.dp).weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                Column { Text(point.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("⌖ ${point.latitude}, ${point.longitude}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 6.dp)); if (point.enderecoTextual.isNotBlank()) Text(point.enderecoTextual, fontSize = 13.sp, color = Color.DarkGray, maxLines = 2, modifier = Modifier.padding(top = 6.dp)) }
                Text(stringResource(R.string.edit_point), color = ExplorerBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}

@Composable private fun EmptyState(onAdd: () -> Unit) { Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("⌖", fontSize = 48.sp, color = Color.Gray); Text(stringResource(R.string.empty_points), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(stringResource(R.string.empty_points_hint), color = Color.Gray, modifier = Modifier.padding(12.dp)); Button(onClick = onAdd) { Text(stringResource(R.string.add_point)) } } }

@Composable
private fun PointFormScreen(initial: PontoTuristico?, onBack: () -> Unit, onSave: (PontoTuristico) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initial?.nome ?: "") }
    var description by remember { mutableStateOf(initial?.descricao ?: "") }
    var latitude by remember { mutableStateOf(initial?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(initial?.longitude?.toString() ?: "") }
    var address by remember { mutableStateOf(initial?.enderecoTextual ?: "") }
    var imageUri by remember { mutableStateOf(initial?.caminhoImagem ?: "") }
    var message by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> if (uri != null) imageUri = uri.toString() }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured -> if (captured) imageUri = cameraUri?.toString().orEmpty() }
    Scaffold(topBar = { ExplorerTopBar(if (initial == null) stringResource(R.string.new_point) else stringResource(R.string.edit_point), onBack) }, bottomBar = { Surface(shadowElevation = 8.dp, color = Color.White) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = onBack) { Text(stringResource(R.string.cancel)) }; Button(onClick = { val lat = latitude.toDoubleOrNull(); val lon = longitude.toDoubleOrNull(); if (name.isBlank() || lat == null || lon == null) message = context.getString(R.string.required_fields) else onSave(PontoTuristico(initial?.id ?: 0, name.trim(), description.trim(), lat, lon, address, imageUri)) }) { Text(stringResource(R.string.save_offline)) } } } }) { padding ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(if (initial == null) stringResource(R.string.new_point_title) else stringResource(R.string.edit_point_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF071E27))
            Text(stringResource(R.string.form_subtitle), color = Color.Gray)
            PhotoPicker(imageUri) { picker.launch("image/*") }
            OutlinedButton(onClick = { cameraUri = createCameraUri(context); cameraUri?.let(camera::launch) }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.take_photo)) }
            FormField(stringResource(R.string.point_name), name, { name = it }, stringResource(R.string.point_name_hint))
            FormField(stringResource(R.string.description), description, { description = it }, stringResource(R.string.description_hint), singleLine = false)
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(stringResource(R.string.location_coordinates), fontWeight = FontWeight.Bold, color = ExplorerGreen); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { FormField(stringResource(R.string.latitude), latitude, { latitude = it }, "-23.5505", Modifier.weight(1f), true); FormField(stringResource(R.string.longitude), longitude, { longitude = it }, "-46.6333", Modifier.weight(1f), true) }; OutlinedButton(onClick = { val lat = latitude.toDoubleOrNull(); val lon = longitude.toDoubleOrNull(); if (lat == null || lon == null) message = context.getString(R.string.coordinates_required) else scope.launch { address = reverseGeocode(context, lat, lon) ?: context.getString(R.string.address_unavailable) } }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.get_address)) }; if (address.isNotBlank()) Text(address, color = Color.DarkGray) } }
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, number: Boolean = false, singleLine: Boolean = true) { Column(modifier) { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray); OutlinedTextField(value, onValueChange, Modifier.fillMaxWidth(), placeholder = { Text(hint) }, singleLine = singleLine, minLines = if (singleLine) 1 else 3, keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Decimal else KeyboardType.Text), shape = RoundedCornerShape(10.dp)) } }

@Composable
private fun PhotoPicker(uri: String, onClick: () -> Unit) { Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFE1EEF5)).clickable { onClick() }, contentAlignment = Alignment.Center) { if (uri.isBlank()) Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("+", fontSize = 44.sp, color = ExplorerBlue); Text(stringResource(R.string.upload_photo), color = ExplorerBlue, fontWeight = FontWeight.Bold); Text(stringResource(R.string.photo_hint), color = Color.Gray, fontSize = 12.sp) } else PointImage(uri, Modifier.fillMaxSize()) } }

@Composable
private fun PointImage(uri: String, modifier: Modifier) { val context = LocalContext.current; val bitmap = remember(uri) { if (uri.isBlank()) null else runCatching { android.graphics.BitmapFactory.decodeStream(context.contentResolver.openInputStream(Uri.parse(uri))) }.getOrNull() }; if (bitmap != null) Image(bitmap.asImageBitmap(), null, modifier, contentScale = ContentScale.Crop) else Box(modifier.background(Color(0xFFE1EEF5)), contentAlignment = Alignment.Center) { Text("⌖", fontSize = 34.sp, color = Color.Gray) } }

@Composable
private fun BottomBar(selectedScreen: Screen, onNavigate: (Screen) -> Unit) { NavigationBar(containerColor = Color(0xFFDBF1FE)) { NavigationBarItem(selectedScreen == Screen.LIST, { onNavigate(Screen.LIST) }, icon = { Text("≡", fontSize = 22.sp) }, label = { Text(stringResource(R.string.points)) }); NavigationBarItem(selected = selectedScreen == Screen.MAP, onClick = { onNavigate(Screen.MAP) }, icon = { Text("⌖", fontSize = 22.sp) }, label = { Text(stringResource(R.string.map)) }); NavigationBarItem(selectedScreen == Screen.SETTINGS, { onNavigate(Screen.SETTINGS) }, icon = { Text("⚙", fontSize = 20.sp) }, label = { Text(stringResource(R.string.settings)) }) } }

private suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) { runCatching { if (!Geocoder.isPresent()) return@withContext null; Geocoder(context).getFromLocation(latitude, longitude, 1)?.firstOrNull()?.getAddressLine(0) }.getOrNull() }

private fun createCameraUri(context: Context): Uri? = runCatching {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "pato_tour_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PatoTour")
    }
    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
}.getOrNull()
 
