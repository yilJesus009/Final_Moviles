package com.example.eventia.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventia.ui.theme.EventiaTheme

/**
 * Pantalla de creación de cuenta (RF-11).
 * Al registrarse el servidor devuelve token y usuario, así que la sesión queda iniciada directamente.
 */
@Composable
fun RegistroScreen(
    onBackClick: () -> Unit,
    onRegistroExitoso: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            onRegistroExitoso()
        }
    }

    RegistroContent(
        uiState = uiState,
        onNombreChange = viewModel::cambiarNombre,
        onEmailChange = viewModel::cambiarEmail,
        onPasswordChange = viewModel::cambiarContrasena,
        onRegisterClick = viewModel::registrarse,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroContent(
    uiState: RegistroUiState,
    onNombreChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre") },
                singleLine = true,
                isError = uiState.errorNombre != null,
                supportingText = uiState.errorNombre?.let { mensaje -> { Text(mensaje) } },
                enabled = !uiState.cargando,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                singleLine = true,
                isError = uiState.errorEmail != null,
                supportingText = uiState.errorEmail?.let { mensaje -> { Text(mensaje) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !uiState.cargando,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.contrasena,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña (mínimo 8 caracteres)") },
                singleLine = true,
                isError = uiState.errorContrasena != null,
                supportingText = uiState.errorContrasena?.let { mensaje -> { Text(mensaje) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !uiState.cargando,
                modifier = Modifier.fillMaxWidth()
            )

            uiState.error?.let { mensaje ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRegisterClick,
                enabled = !uiState.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (uiState.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Crear cuenta")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistroContentPreview() {
    EventiaTheme {
        RegistroContent(
            uiState = RegistroUiState(
                nombre = "Ana Gómez",
                email = "ana@eventia.test"
            ),
            onNombreChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onRegisterClick = {},
            onBackClick = {}
        )
    }
}
