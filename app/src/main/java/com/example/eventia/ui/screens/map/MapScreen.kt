package com.example.eventia.ui.screens.map

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventia.models.Evento
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.time.format.DateTimeFormatter
import java.util.Locale

// Centro por defecto (Bolivia) antes de centrar en el primer evento con coordenadas
private val PUNTO_INICIAL = GeoPoint(-17.3935, -66.1570)

/**
 * Pantalla del Mapa de Eventos (Módulo E · RF-07 y RF-08).
 * - Dibuja marcadores únicamente para eventos con coordenadas válidas.
 * - Respeta el filtro de categoría.
 * - Al tocar un marcador muestra una hoja inferior con el resumen y un botón para ir al detalle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onEventClick: (Int) -> Unit = {},
    viewModel: MapaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mapa de Eventos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = { viewModel.cargarEventos() },
                enabled = !uiState.cargando
            ) {
                Text("Actualizar")
            }
        }

        // Aviso cuando los eventos vienen de la caché sin conexión (RF-14)
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
        }

        // RF-07: filtro por categoría (mismo estilo que en el Catálogo)
        if (uiState.categorias.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.categoriaSeleccionadaId == null,
                        onClick = { viewModel.seleccionarCategoria(null) },
                        label = { Text("Todas") }
                    )
                }
                items(uiState.categorias, key = { it.id }) { categoria ->
                    FilterChip(
                        selected = uiState.categoriaSeleccionadaId == categoria.id,
                        onClick = { viewModel.seleccionarCategoria(categoria.id) },
                        label = { Text(categoria.nombre) }
                    )
                }
            }
        }

        // Estados de la pantalla (sección 5 y 6.5: carga, error, vacío y contenido)
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.cargando && uiState.eventosConUbicacion.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando mapa de eventos...")
                    }
                }

                uiState.error != null && uiState.eventosConUbicacion.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "Error al cargar el mapa",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.cargarEventos() }) {
                            Text("Reintentar")
                        }
                    }
                }

                uiState.eventosConUbicacion.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay eventos con ubicación para mostrar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        if (uiState.categoriaSeleccionadaId != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.seleccionarCategoria(null) }) {
                                Text("Ver todas las categorías")
                            }
                        }
                    }
                }

                else -> {
                    MapaOsm(
                        eventos = uiState.eventosConUbicacion,
                        onMarcadorClick = { evento -> viewModel.seleccionarEvento(evento) }
                    )
                }
            }
        }
    }

    // RF-08: hoja inferior con el resumen del evento seleccionado
    uiState.eventoSeleccionado?.let { evento ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.seleccionarEvento(null) },
            sheetState = rememberModalBottomSheetState()
        ) {
            ResumenEventoSheet(
                evento = evento,
                onVerDetalleClick = {
                    val id = evento.id
                    viewModel.seleccionarEvento(null)
                    onEventClick(id)
                }
            )
        }
    }
}

/**
 * Vista de OpenStreetMap (osmdroid) envuelta en AndroidView para usarla desde Compose.
 */
@Composable
private fun MapaOsm(
    eventos: List<Evento>,
    onMarcadorClick: (Evento) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var ultimoConjuntoIds by remember { mutableStateOf<List<Int>>(emptyList()) }

    // osmdroid exige definir un userAgent antes de crear el MapView para poder descargar los mosaicos
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.5)
            controller.setCenter(PUNTO_INICIAL)
        }
    }

    // Conecta el ciclo de vida de la pantalla con el MapView (onResume / onPause)
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Mapa con ${eventos.size} eventos" },
        update = { mapa ->
            mapa.overlays.clear()

            eventos.forEach { evento ->
                val lugar = evento.lugar ?: return@forEach
                val lat = lugar.latitud ?: return@forEach
                val lng = lugar.longitud ?: return@forEach

                val marcador = Marker(mapa).apply {
                    position = GeoPoint(lat, lng)
                    title = evento.titulo
                    snippet = lugar.nombre
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onMarcadorClick(evento)
                        true
                    }
                }
                mapa.overlays.add(marcador)
            }

            // Solo recentra la cámara cuando cambia la lista de eventos (no en cada recomposición)
            val idsActuales = eventos.map { it.id }
            if (idsActuales != ultimoConjuntoIds) {
                ultimoConjuntoIds = idsActuales
                eventos.firstOrNull()?.lugar?.let { primero ->
                    val lat = primero.latitud
                    val lng = primero.longitud
                    if (lat != null && lng != null) {
                        mapa.controller.animateTo(GeoPoint(lat, lng))
                    }
                }
            }

            mapa.invalidate()
        }
    )
}

@Composable
private fun ResumenEventoSheet(
    evento: Evento,
    onVerDetalleClick: () -> Unit
) {
    val formateadorFecha = remember {
        DateTimeFormatter.ofPattern("EEE d MMM yyyy · HH:mm", Locale.forLanguageTag("es-BO"))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        evento.categoria?.let { categoria ->
            AssistChip(
                onClick = {},
                label = { Text(categoria.nombre) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = evento.titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        evento.fechaInicio?.let { fecha ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📅 ${fecha.format(formateadorFecha)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        evento.lugar?.let { lugar ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📍 ${lugar.nombre}" +
                    if (lugar.direccion.isNotBlank()) " · ${lugar.direccion}" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (evento.resumen.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = evento.resumen,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onVerDetalleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Ver detalle del evento")
        }
    }
}
