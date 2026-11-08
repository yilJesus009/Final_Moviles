package com.example.eventia.ui.screens.venues

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.eventia.ui.components.TarjetaEvento
import kotlinx.coroutines.launch

/**
 * Pantalla de Detalle del Lugar (Módulo B: RF-13 y RF-09).
 * Presenta imagen, dirección, descripción, horario, botón de indicaciones externas
 * y la lista de próximos eventos del lugar reutilizando [TarjetaEvento].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleLugarScreen(
    onBackClick: () -> Unit,
    onEventClick: (Int) -> Unit,
    viewModel: DetalleLugarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Lugar") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Volver")
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
                        Text("Cargando información del lugar...")
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
                            text = uiState.error ?: "Error al cargar el lugar",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.cargarDetalleLugar() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            uiState.lugar != null -> {
                val lugar = uiState.lugar!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cabecera del lugar
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = lugar.imagenUrl,
                                contentDescription = "Imagen de ${lugar.nombre}",
                                placeholder = painterResource(id = R.drawable.ic_evento_placeholder),
                                error = painterResource(id = R.drawable.ic_evento_placeholder),
                                fallback = painterResource(id = R.drawable.ic_evento_placeholder),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = lugar.nombre,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                if (lugar.direccion.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "📍 ${lugar.direccion}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (lugar.horario.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "🕒 Horario: ${lugar.horario}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (lugar.descripcion.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = lugar.descripcion,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // RF-09 y RF-13: Botón de indicaciones en app externa de mapas
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

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Próximos eventos en este lugar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Lista de próximos eventos reutilizando TarjetaEvento (RF-13)
                    if (uiState.eventosProximos.isEmpty()) {
                        item {
                            Text(
                                text = "No hay eventos próximos programados en este lugar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        items(
                            items = uiState.eventosProximos,
                            key = { it.id }
                        ) { evento ->
                            TarjetaEvento(
                                evento = evento,
                                onClick = { onEventClick(evento.id) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
