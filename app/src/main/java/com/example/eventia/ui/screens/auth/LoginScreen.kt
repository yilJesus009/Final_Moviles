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
 * Pantalla de inicio de sesión (RF-11).
 * Al iniciar sesión correctamente se llama a [onLoginExitoso], que regresa a la pantalla anterior
 * (por ejemplo, al detalle del evento si el usuario venía de intentar inscribirse - RF-12).
 */
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginExitoso: () -> Unit,
    onRegistroClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.sesionIniciada) {
        if (uiState.sesionIniciada) {
            onLoginExitoso()
        }
    }

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::cambiarEmail,
        onPasswordChange = viewModel::cambiarContrasena,
        onLoginClick = viewModel::iniciarSesion,
        onBackClick = onBackClick,
        onRegistroClick = onRegistroClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
    onRegistroClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") },
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
                text = "Bienvenido a Eventia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Inicia sesión para inscribirte a eventos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

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
                label = { Text("Contraseña") },
                singleLine = true,
                isError = uiState.errorContrasena != null,
                supportingText = uiState.errorContrasena?.let { mensaje -> { Text(mensaje) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !uiState.cargando,
                modifier = Modifier.fillMaxWidth()
            )

            // Error general (servidor o conexión)
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
                onClick = onLoginClick,
                enabled = !uiState.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (uiState.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Iniciar sesión")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRegistroClick, enabled = !uiState.cargando) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentPreview() {
    EventiaTheme {
        LoginContent(
            uiState = LoginUiState(email = "ana@eventia.test"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onBackClick = {},
            onRegistroClick = {}
        )
    }
}
