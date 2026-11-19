package com.example.eventia.ui.screens.venues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventia.models.Lugar
import com.example.eventia.ui.components.TarjetaLugar
import com.example.eventia.ui.theme.EventiaTheme

/**
 * Pantalla de Lista de Lugares (Módulo B: RF-13).
 * Muestra todos los lugares con su nombre, dirección, imagen y cantidad de eventos próximos.
 */
@Composable
fun LugaresScreen(
    onBackClick: () -> Unit,
    onVenueClick: (Int) -> Unit,
    viewModel: LugaresViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LugaresContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onVenueClick = onVenueClick,
        onRetry = viewModel::cargarLugares
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LugaresContent(
    uiState: LugaresUiState,
    onBackClick: () -> Unit,
    onVenueClick: (Int) -> Unit,
    onRetry: () -> Unit
) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lugares") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando lugares...")
                    }
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            uiState.lugares.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay lugares registrados en este momento.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.lugares,
                        key = { it.id }
                    ) { lugar ->
                        TarjetaLugar(
                            lugar = lugar,
                            onClick = { onVenueClick(lugar.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LugaresContentPreview() {
    EventiaTheme {
        LugaresContent(
            uiState = LugaresUiState(
                lugares = listOf(
                    Lugar(
                        id = 1,
                        nombre = "Centro Cultural Franco Alemán",
                        direccion = "Av. Velarde #200, Santa Cruz de la Sierra",
                        latitud = -17.7912,
                        longitud = -63.1814,
                        cantidadEventosProximos = 2
                    ),
                    Lugar(
                        id = 2,
                        nombre = "Manzana Uno Espacio de Arte",
                        direccion = "Calle Independencia esq. Ayacucho, Plaza 24 de Septiembre",
                        latitud = -17.7838,
                        longitud = -63.1816,
                        cantidadEventosProximos = 3
                    )
                )
            ),
            onBackClick = {},
            onVenueClick = {},
            onRetry = {}
        )
    }
}
