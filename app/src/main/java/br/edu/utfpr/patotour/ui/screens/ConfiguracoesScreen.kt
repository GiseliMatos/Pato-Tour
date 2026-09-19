package br.edu.utfpr.patotour.ui.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.utfpr.patotour.preferences.ConfiguracoesPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import br.edu.utfpr.patotour.storage.Cache

@Composable
fun ConfiguracoesScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val configuracoesPreferences = remember {
        ConfiguracoesPreferences(context)
    }

    var zoomPadrao by remember {
        mutableFloatStateOf(configuracoesPreferences.obterZoom())
    }

    var tipoMapa by remember {
        mutableStateOf(configuracoesPreferences.obterTipoMapa())
    }

    var tamanhoCache by remember {
        mutableStateOf("Calculando...")
    }

    var mensagemCache by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        tamanhoCache = withContext(Dispatchers.IO) {
            Cache.obterTamanhoCache(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Preferências do Mapa",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Zoom padrão",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Nível atual: ${zoomPadrao.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = zoomPadrao,
                    onValueChange = {
                        zoomPadrao = it
                        configuracoesPreferences.salvarZoom(it)
                    },
                    valueRange = 5f..20f,
                    steps = 14
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tipo de mapa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    TipoMapaButton(
                        texto = "Rodoviário",
                        selecionado = tipoMapa == "Rodoviário",
                        onClick = {
                            tipoMapa = "Rodoviário"
                            configuracoesPreferences.salvarTipoMapa("Rodoviário")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TipoMapaButton(
                        texto = "Satélite",
                        selecionado = tipoMapa == "Satélite",
                        onClick = {
                            tipoMapa = "Satélite"
                            configuracoesPreferences.salvarTipoMapa("Satélite")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    TipoMapaButton(
                        texto = "Híbrido",
                        selecionado = tipoMapa == "Híbrido",
                        onClick = {
                            tipoMapa = "Híbrido"
                            configuracoesPreferences.salvarTipoMapa("Híbrido")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TipoMapaButton(
                        texto = "Terreno",
                        selecionado = tipoMapa == "Terreno",
                        onClick = {
                            tipoMapa = "Terreno"
                            configuracoesPreferences.salvarTipoMapa("Terreno")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Dados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Armazenamento local",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Cache atual: $tamanhoCache",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Arquivos temporários utilizados pelo aplicativo podem ser removidos sem excluir os pontos turísticos cadastrados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {

                            tamanhoCache = withContext(Dispatchers.IO) {
                                Cache.limparCache(context)
                                Cache.obterTamanhoCache(context)
                            }

                            mensagemCache = "Cache limpo com sucesso."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Limpar cache")
                }
            }
            mensagemCache?.let {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TipoMapaButton(
    texto: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selecionado) {
        Button(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(texto)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(texto)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfiguracoesScreenPreview() {
    MaterialTheme {
        ConfiguracoesScreen()
    }
}