package com.example.eventia.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventia.ui.components.TarjetaEvento

/**
 * Pantalla del Catálogo de Eventos (Módulo A y C: RF-01, RF-02, RF-03, RF-04, RF-06, RF-14).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onEventClick: (Int) -> Unit = {},
    onVenuesClick: () -> Unit = {},
    viewModel: CatalogoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    // RF-02: conserva la posición de la lista al volver desde el detalle
    val listState = rememberLazyListState()

    // RF-06 y RF-04: muestra avisos no bloqueantes en Snackbar
    LaunchedEffect(uiState.mensajeAviso) {
        uiState.mensajeAviso?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.consumirMensajeAviso()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cabecera con título, acceso a Lugares y botón de actualización manual (RF-06)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Explorar Eventos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onVenuesClick) {
                        Text("Lugares")
                    }
                    TextButton(
                        onClick = { viewModel.actualizarCatalogo() },
                        enabled = !uiState.cargando && !uiState.actualizando
                    ) {
                        Text(if (uiState.actualizando) "Actualizando..." else "Actualizar")
                    }
                }
            }

            // RF-14 y RF-05: Aviso visible cuando se navega sin conexión con datos de caché
            uiState.avisoSinConexion?.let { aviso ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "⚡ $aviso",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // RF-03: Barra de búsqueda con debounce de 300 ms
            OutlinedTextField(
                value = uiState.textoBusqueda,
                onValueChange = { viewModel.cambiarTextoBusqueda(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Buscar eventos por nombre...") },
                singleLine = true,
                trailingIcon = {
                    if (uiState.textoBusqueda.isNotEmpty()) {
                        TextButton(onClick = { viewModel.cambiarTextoBusqueda("") }) {
                            Text("✕")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // RF-03: Filtro por categorías + botón "Limpiar filtros"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.categoriaSeleccionadaId == null,
                            onClick = { viewModel.seleccionarCategoria(null) },
                            label = { Text("Todas") }
                        )
                    }
                    items(
                        items = uiState.categorias,
                        key = { it.id }
                    ) { categoria ->
                        FilterChip(
                            selected = uiState.categoriaSeleccionadaId == categoria.id,
                            onClick = { viewModel.seleccionarCategoria(categoria.id) },
                            label = { Text(categoria.nombre) }
                        )
                    }
                }

                if (uiState.tieneFiltrosActivos) {
                    TextButton(
                        onClick = { viewModel.limpiarFiltros() }
                    ) {
                        Text("Limpiar filtros")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // RF-06: Pull-to-refresh envolviendo los estados de carga, error, vacío y contenido
            PullToRefreshBox(
                isRefreshing = uiState.actualizando,
                onRefresh = { viewModel.actualizarCatalogo() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    // 1. Estado de carga inicial
                    uiState.cargando && uiState.eventos.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Cargando eventos...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // 2. Estado de error (solo si no hay eventos previos ni en caché)
                    uiState.error != null && uiState.eventos.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = uiState.error ?: "Ocurrió un error al cargar el catálogo",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.cargarEventos() }) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }

                    // 3. Estado de lista vacía (con o sin filtros activos)
                    uiState.eventos.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (uiState.tieneFiltrosActivos) {
                                        "No se encontraron eventos que coincidan con tu búsqueda o categoría."
                                    } else {
                                        "No hay próximos eventos disponibles en este momento."
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (uiState.tieneFiltrosActivos) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(onClick = { viewModel.limpiarFiltros() }) {
                                        Text("Limpiar filtros")
                                    }
                                }
                            }
                        }
                    }

                    // 4. Estado con contenido (lista de tarjetas de eventos)
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.eventos,
                                key = { it.id }
                            ) { evento ->
                                val esFavorito = evento.id in uiState.idsFavoritos
                                TarjetaEvento(
                                    evento = evento,
                                    onClick = { onEventClick(evento.id) },
                                    accionDerecha = {
                                        TextButton(
                                            onClick = { viewModel.alternarFavorito(evento) },
                                            modifier = Modifier.semantics {
                                                contentDescription = if (esFavorito) {
                                                    "Quitar ${evento.titulo} de favoritos"
                                                } else {
                                                    "Agregar ${evento.titulo} a favoritos"
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = if (esFavorito) "♥ Favorito" else "♡ Favorito",
                                                color = if (esFavorito) {
                                                    MaterialTheme.colorScheme.error
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
