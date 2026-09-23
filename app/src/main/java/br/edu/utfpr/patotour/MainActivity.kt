 package br.edu.utfpr.patotour

 import android.os.Bundle
 import androidx.activity.ComponentActivity
 import androidx.activity.compose.rememberLauncherForActivityResult
 import androidx.activity.compose.setContent
 import androidx.activity.enableEdgeToEdge
 import androidx.compose.foundation.layout.fillMaxSize
 import androidx.compose.material3.MaterialTheme
 import androidx.compose.material3.Surface
 import androidx.compose.runtime.Composable
 import androidx.compose.runtime.collectAsState
 import androidx.compose.runtime.getValue
 import androidx.compose.runtime.mutableStateOf
 import androidx.compose.runtime.remember
 import androidx.compose.runtime.saveable.rememberSaveable
 import androidx.compose.runtime.setValue
 import androidx.compose.ui.Modifier
 import androidx.lifecycle.viewmodel.compose.viewModel
 import androidx.lifecycle.viewmodel.initializer
 import androidx.lifecycle.viewmodel.viewModelFactory
 import br.edu.utfpr.patotour.data.model.PontoTuristico
 import br.edu.utfpr.patotour.data.repository.PontoTuristicoRepository
 import br.edu.utfpr.patotour.ui.screens.ConfiguracoesScreen
 import br.edu.utfpr.patotour.ui.screens.PointFormScreen
 import br.edu.utfpr.patotour.ui.screens.PointsScreen
 import br.edu.utfpr.patotour.ui.theme.PatoTourTheme
 import br.edu.utfpr.patotour.ui.viewmodel.PontoTuristicoViewModel

 class MainActivity : ComponentActivity() {
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         enableEdgeToEdge()
         val repository = (application as PatoTourApplication).pontoTuristicoRepository
         setContent {
             PatoTourTheme {
                 PatoTourApp(repository = repository)
             }
         }
     }
 }

 private enum class Screen { LIST, FORM, SETTINGS }

 @Composable
 fun PatoTourApp(repository: PontoTuristicoRepository) {
     val viewModel: PontoTuristicoViewModel = viewModel(
         factory = viewModelFactory {
             initializer {
                 PontoTuristicoViewModel(repository)
             }
         }
     )
     val points by viewModel.points.collectAsState()

     var screen by rememberSaveable { mutableStateOf(Screen.LIST) }
     var editing by remember { mutableStateOf<PontoTuristico?>(null) }

     Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
         when (screen) {
             Screen.LIST -> PointsScreen(
                 points = points,
                 onAdd = { editing = null; screen = Screen.FORM },
                 onEdit = { editing = it; screen = Screen.FORM },
                 onSettingsClick = { screen = Screen.SETTINGS }
             )
             Screen.FORM -> PointFormScreen(
                 initial = editing,
                 onBack = { screen = Screen.LIST },
                 onSave = { point ->
                     viewModel.salvar(point)
                     screen = Screen.LIST
                 }
             )
             Screen.SETTINGS -> ConfiguracoesScreen(
                 onPointsClick = {
                     screen = Screen.LIST
                 }
             )
         }
     }
 }