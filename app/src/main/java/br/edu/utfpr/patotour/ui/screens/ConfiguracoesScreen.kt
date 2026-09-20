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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.utfpr.patotour.R
import br.edu.utfpr.patotour.preferences.ConfiguracoesPreferences
import br.edu.utfpr.patotour.storage.Armazenamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ConfiguracoesScreen() {

    val context = LocalContext.current

    val configuracoesPreferences = remember {
        ConfiguracoesPreferences(context)
    }

    var zoomPadrao by remember {
        mutableFloatStateOf(configuracoesPreferences.obterZoom())
    }

    var tipoMapa by remember {
        mutableStateOf(configuracoesPreferences.obterTipoMapa())
    }

    var tamanhoArmazenamento by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        tamanhoArmazenamento = withContext(Dispatchers.IO) {
            Armazenamento.obterTamanhoUtilizado(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.map_preferences),
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
                    text = stringResource(R.string.default_zoom),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = stringResource(
                        R.string.current_level,
                        zoomPadrao.toInt()
                    ),
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
                    text = stringResource(R.string.map_type),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    TipoMapaButton(
                        texto = stringResource(R.string.map_roadmap),
                        selecionado = tipoMapa == "Rodoviário",
                        onClick = {
                            tipoMapa = "Rodoviário"
                            configuracoesPreferences.salvarTipoMapa("Rodoviário")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TipoMapaButton(
                        texto = stringResource(R.string.map_satellite),
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
                        texto = stringResource(R.string.map_hybrid),
                        selecionado = tipoMapa == "Híbrido",
                        onClick = {
                            tipoMapa = "Híbrido"
                            configuracoesPreferences.salvarTipoMapa("Híbrido")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TipoMapaButton(
                        texto = stringResource(R.string.map_terrain),
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
            text = stringResource(R.string.data),
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
                    text = stringResource(R.string.local_storage),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (tamanhoArmazenamento.isBlank()) {
                        stringResource(R.string.calculating)
                    } else {
                        stringResource(
                            R.string.storage_used,
                            tamanhoArmazenamento
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.storage_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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