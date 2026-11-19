package com.example.eventia.ui.screens.favorites

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventia.models.Categoria
import com.example.eventia.models.Evento
import com.example.eventia.models.Lugar
import com.example.eventia.ui.components.TarjetaEvento
import com.example.eventia.ui.theme.EventiaTheme
import java.time.ZonedDateTime

/**
 * Pantalla de Eventos Favoritos (Módulo C: RF-04, RF-05, RF-13).
 * - Funciona 100% sin conexión leyendo de Room.
 * - Muestra un indicador claro cuando el dispositivo está sin conexión (RF-05).
 * - Al quitar un favorito ofrece "Deshacer" mediante un Snackbar (RF-04).
 * - Reutiliza el componente [TarjetaEvento] (RF-13).
 */
@Composable
fun FavoritesScreen(
    onEventClick: (Int) -> Unit = {},
    viewModel: FavoritosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val sinConexion = rememberEstadoSinConexion(context)

    // RF-04: Al quitar un favorito, muestra un Snackbar con la acción "Deshacer"
    LaunchedEffect(uiState.eventoEliminadoReciente) {
        val eliminado = uiState.eventoEliminadoReciente ?: return@LaunchedEffect
        val resultado = snackbarHostState.showSnackbar(
            message = "Se quitó \"${eliminado.titulo}\" de favoritos",
            actionLabel = "Deshacer",
            duration = SnackbarDuration.Short
        )
        if (resultado == SnackbarResult.ActionPerformed) {
            viewModel.deshacerEliminacion()
        } else {
            viewModel.limpiarEliminadoReciente()
        }
    }

    FavoritesContent(
        uiState = uiState,
        sinConexion = sinConexion,
        snackbarHostState = snackbarHostState,
        onEventClick = onEventClick,
        onRemoveFavorite = viewModel::quitarFavorito
    )
}

@Composable
fun FavoritesContent(
    uiState: FavoritosUiState,
    sinConexion: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEventClick: (Int) -> Unit = {},
    onRemoveFavorite: (Evento) -> Unit = {}
) {
    val listState = rememberLazyListState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Mis Favoritos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // RF-05: Aviso claro cuando el dispositivo no tiene conexión a Internet
            if (sinConexion) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "⚡ Sin conexión · tus favoritos guardados están disponibles offline",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            when {
                uiState.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.favoritos.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Aún no tienes eventos favoritos guardados.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Marca eventos con ♥ en el catálogo o en su detalle para consultarlos incluso sin conexión.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.favoritos,
                            key = { it.id }
                        ) { evento ->
                            TarjetaEvento(
                                evento = evento,
                                onClick = { onEventClick(evento.id) },
                                accionDerecha = {
                                    TextButton(
                                        onClick = { onRemoveFavorite(evento) },
                                        modifier = Modifier.semantics {
                                            contentDescription = "Quitar ${evento.titulo} de favoritos"
                                        }
                                    ) {
                                        Text(
                                            text = "♥ Quitar",
                                            color = MaterialTheme.colorScheme.error
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

@Preview(showBackground = true)
@Composable
private fun FavoritesContentPreview() {
    EventiaTheme {
        FavoritesContent(
            uiState = FavoritosUiState(
                favoritos = listOf(
                    Evento(
                        id = 1,
                        titulo = "Noche de jazz en la plaza",
                        resumen = "Clásicos del jazz con músicos invitados en vivo.",
                        descripcion = "",
                        fechaInicio = ZonedDateTime.parse("2026-11-20T20:00:00-04:00"),
                        imagenUrl = null,
                        categoria = Categoria(3, "Música"),
                        lugar = Lugar(3, "Casa Municipal de la Cultura", "Libertad #65", -17.78, -63.18),
                        capacidad = 50,
                        cuposDisponibles = 36
                    )
                )
            ),
            sinConexion = true
        )
    }
}

/**
 * Observa en tiempo real si el dispositivo tiene conexión a Internet activa (RF-05).
 */
@Composable
private fun rememberEstadoSinConexion(context: Context): Boolean {
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun comprobarSinConexion(): Boolean {
        val redActiva = connectivityManager.activeNetwork ?: return true
        val capacidades = connectivityManager.getNetworkCapabilities(redActiva) ?: return true
        return !capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    var sinConexion by remember { mutableStateOf(comprobarSinConexion()) }

    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                sinConexion = false
            }

            override fun onLost(network: Network) {
                sinConexion = comprobarSinConexion()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                sinConexion = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return sinConexion
}
