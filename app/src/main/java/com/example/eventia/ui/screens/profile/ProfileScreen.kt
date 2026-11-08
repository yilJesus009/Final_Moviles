package com.example.eventia.ui.screens.profile

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventia.models.Evento
import com.example.eventia.models.Usuario
import com.example.eventia.ui.components.TarjetaEvento

/**
 * Pantalla de Perfil (RF-11 y RF-12).
 * - Sin sesión: invita a iniciar sesión o crear cuenta (el resto de la app funciona sin cuenta).
 * - Con sesión: datos del usuario, "Mis inscripciones" (próximas y pasadas) y cerrar sesión.
 */
@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit = {},
    onRegistroClick: () -> Unit = {},
    onEventClick: (Int) -> Unit = {},
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // RF-11: si el servidor respondió 401, la sesión local ya se borró → llevar al inicio de sesión
    LaunchedEffect(uiState.sesionExpirada) {
        if (uiState.sesionExpirada) {
            viewModel.consumirSesionExpirada()
            onLoginClick()
        }
    }

    // RF-12: al volver a la pestaña de Perfil (con sesión activa), refresca "Mis inscripciones"
    LaunchedEffect(uiState.usuario?.id) {
        if (uiState.usuario != null) {
            viewModel.cargarDatos()
        }
    }

    val usuario = uiState.usuario
    if (usuario == null) {
        SinSesion(onLoginClick = onLoginClick, onRegistroClick = onRegistroClick)
    } else {
        ConSesion(
            usuario = usuario,
            uiState = uiState,
            onEventClick = onEventClick,
            onReintentar = { viewModel.cargarDatos() },
            onCerrarSesion = { viewModel.cerrarSesion() }
        )
    }
}

@Composable
private fun SinSesion(
    onLoginClick: () -> Unit,
    onRegistroClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Inicia sesión para inscribirte a eventos y ver tus inscripciones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Iniciar sesión")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onRegistroClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Crear cuenta")
            }
        }
    }
}

@Composable
private fun ConSesion(
    usuario: Usuario,
    uiState: PerfilUiState,
    onEventClick: (Int) -> Unit,
    onReintentar: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Datos del usuario y cerrar sesión
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = usuario.nombre.ifBlank { "Usuario" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = usuario.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onCerrarSesion) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }

        item {
            Text(
                text = "Mis inscripciones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        when {
            uiState.cargando -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onReintentar) {
                        Text("Reintentar")
                    }
                }
            }

            uiState.proximas.isEmpty() && uiState.pasadas.isEmpty() -> item {
                Text(
                    text = "Todavía no te inscribiste a ningún evento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                seccionInscripciones("Próximas", uiState.proximas, onEventClick)
                seccionInscripciones("Pasadas", uiState.pasadas, onEventClick)
            }
        }
    }
}

// Agrega a la lista un subtítulo y las tarjetas de la sección (reutiliza TarjetaEvento - RF-13)
private fun androidx.compose.foundation.lazy.LazyListScope.seccionInscripciones(
    titulo: String,
    eventos: List<Evento>,
    onEventClick: (Int) -> Unit
) {
    item {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$titulo (${eventos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    if (eventos.isEmpty()) {
        item {
            Text(
                text = "Sin eventos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        items(eventos, key = { "$titulo-${it.id}" }) { evento ->
            TarjetaEvento(
                evento = evento,
                onClick = { onEventClick(evento.id) }
            )
        }
    }
}
