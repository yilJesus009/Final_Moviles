package com.example.eventia.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventia.R
import com.example.eventia.models.Evento
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Componente reutilizable de tarjeta de evento (RF-01 y RF-13).
 * Se usa en el Catálogo, en Favoritos y en el Detalle del Lugar.
 */
@Composable
fun TarjetaEvento(
    evento: Evento,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accionDerecha: (@Composable () -> Unit)? = null
) {
    val formateadorFecha = remember {
        DateTimeFormatter.ofPattern("EEE d MMM yyyy · HH:mm", Locale.forLanguageTag("es-BO"))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // RF-01: Imagen del evento o imagen sustituta si imagenUrl es nulo o falla
            AsyncImage(
                model = evento.imagenUrl,
                contentDescription = "Imagen del evento ${evento.titulo}",
                placeholder = painterResource(id = R.drawable.ic_evento_placeholder),
                error = painterResource(id = R.drawable.ic_evento_placeholder),
                fallback = painterResource(id = R.drawable.ic_evento_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.medium)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Categoría (si existe)
                    evento.categoria?.let { categoria ->
                        AssistChip(
                            onClick = onClick,
                            label = {
                                Text(
                                    text = categoria.nombre,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    } ?: Spacer(modifier = Modifier.height(1.dp))

                    // Espacio opcional para botón de favorito o acción secundaria
                    accionDerecha?.invoke()
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Nombre / Título del evento
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Resumen del evento
                if (evento.resumen.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = evento.resumen,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fecha y hora en zona horaria local
                evento.fechaInicio?.let { fecha ->
                    Text(
                        text = "📅 ${fecha.format(formateadorFecha)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Lugar del evento
                evento.lugar?.let { lugar ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍 ${lugar.nombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
