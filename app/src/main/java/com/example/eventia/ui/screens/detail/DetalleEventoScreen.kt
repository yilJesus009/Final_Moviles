package com.example.eventia.ui.screens.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.eventia.R
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla de Detalle del Evento (RF-02, RF-04, RF-05, RF-09, RF-12, RF-13).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEventoScreen(
    onBackClick: () -> Unit,
    onVenueClick: (Int) -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: DetalleEventoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val formateadorFecha = remember {
        DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy · HH:mm", Locale.forLanguageTag("es-BO"))
    }

    LaunchedEffect(uiState.mensajeAviso) {
        uiState.mensajeAviso?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.consumirMensajeAviso()
        }
    }

    LaunchedEffect(uiState.navegarALogin) {
        if (uiState.navegarALogin) {
            viewModel.consumirNavegarALogin()
            onLoginClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Evento") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Volver")
                    }
                },
                actions = {
                    if (uiState.evento != null) {
                        TextButton(onClick = { viewModel.alternarFavorito() }) {
                            Text(
                                text = if (uiState.esFavorito) "♥ Favorito" else "♡ Favorito",
                                color = if (uiState.esFavorito) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                        Text("Cargando detalle del evento...")
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
                            text = uiState.error ?: "Error al cargar el evento",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.noEncontrado) {
                            Button(onClick = onBackClick) {
                                Text("Volver")
                            }
                        } else {
                            Button(onClick = { viewModel.cargarDetalle() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }

            uiState.evento != null -> {
                val evento = uiState.evento!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // RF-05 y RF-14: Aviso visible cuando se consulta sin conexión
                    uiState.avisoSinConexion?.let { aviso ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚡ $aviso",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    AsyncImage(
                        model = evento.imagenUrl,
                        contentDescription = "Imagen de ${evento.titulo}",
                        placeholder = painterResource(id = R.drawable.ic_evento_placeholder),
                        error = painterResource(id = R.drawable.ic_evento_placeholder),
                        fallback = painterResource(id = R.drawable.ic_evento_placeholder),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
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
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        evento.fechaInicio?.let { fecha ->
                            Text(
                                text = "📅 ${fecha.format(formateadorFecha)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Cupos disponibles e inscripción (RF-02 / RF-12)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Cupos disponibles",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (evento.tieneCupos) {
                                            "${evento.cuposDisponibles} de ${evento.capacidad}"
                                        } else {
                                            "Sin cupos disponibles"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (evento.tieneCupos) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Botón de inscripción que cambia de estado según sesión y cupos (RF-12)
                                val puedeActuar = !uiState.procesandoInscripcion &&
                                    (evento.estaInscrito || evento.tieneCupos || !uiState.haySesion)

                                if (evento.estaInscrito) {
                                    OutlinedButton(
                                        onClick = { viewModel.gestionarInscripcion() },
                                        enabled = puedeActuar,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        if (uiState.procesandoInscripcion) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text("Inscrito · Cancelar inscripción")
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.gestionarInscripcion() },
                                        enabled = puedeActuar,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        if (uiState.procesandoInscripcion) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            val textoBoton = when {
                                                !uiState.haySesion -> "Inicia sesión para inscribirte"
                                                evento.tieneCupos -> "Inscribirme a este evento"
                                                else -> "Evento sin cupos disponibles"
                                            }
                                            Text(textoBoton)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lugar e indicaciones externas (RF-02, RF-09, RF-13)
                        evento.lugar?.let { lugar ->
                            Text(
                                text = "Lugar del evento",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { onVenueClick(lugar.id) },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "📍 ${lugar.nombre}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (lugar.direccion.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = lugar.direccion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // RF-09: Abrir aplicación externa de mapas de forma segura
                                    OutlinedButton(
                                        onClick = {
                                            val uri = if (lugar.tieneCoordenadasValidas) {
                                                Uri.parse(
                                                    "geo:${lugar.latitud},${lugar.longitud}?q=${lugar.latitud},${lugar.longitud}(${Uri.encode(lugar.nombre)})"
                                                )
                                            } else {
                                                Uri.parse("geo:0,0?q=${Uri.encode("${lugar.nombre} ${lugar.direccion}")}")
                                            }
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: ActivityNotFoundException) {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        "No se encontró ninguna aplicación de mapas instalada en el dispositivo."
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Cómo llegar (Abrir en Mapas)")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = evento.descripcion.ifBlank { evento.resumen },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
