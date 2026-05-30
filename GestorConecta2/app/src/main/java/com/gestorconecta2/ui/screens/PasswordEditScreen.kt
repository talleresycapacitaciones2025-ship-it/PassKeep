package com.gestorconecta2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.gestorconecta2.domain.model.PasswordEntry
import com.gestorconecta2.ui.components.PasswordField
import com.gestorconecta2.ui.components.PasswordGeneratorDialog
import com.gestorconecta2.ui.components.PasswordStrengthIndicator
import com.gestorconecta2.ui.theme.MonospaceFontFamily
import com.gestorconecta2.util.PasswordStrength
import androidx.compose.foundation.text.KeyboardOptions

/**
 * Pantalla para añadir o editar una contraseña.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordEditScreen(
    entry: PasswordEntry?,
    onSave: (PasswordEntry) -> Unit,
    onCancel: () -> Unit,
    onGeneratePassword: () -> String,
    getPasswordStrength: (String) -> PasswordStrength,
    modifier: Modifier = Modifier
) {
    var serviceName by remember { mutableStateOf(entry?.serviceName ?: "") }
    var username by remember { mutableStateOf(entry?.username ?: "") }
    var password by remember { mutableStateOf(entry?.encryptedPassword ?: "") }
    var notes by remember { mutableStateOf(entry?.notes ?: "") }
    var category by remember { mutableStateOf(entry?.category ?: "") }

    var showGeneratorDialog by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val strength = remember(password) { getPasswordStrength(password) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entry == null) "Nueva Contraseña" else "Editar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val updatedEntry = entry?.copy(
                                serviceName = serviceName,
                                username = username,
                                encryptedPassword = password,
                                notes = notes,
                                category = category
                            ) ?: PasswordEntry(
                                serviceName = serviceName,
                                username = username,
                                encryptedPassword = password,
                                notes = notes,
                                category = category
                            )
                            onSave(updatedEntry)
                        },
                        enabled = serviceName.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre del servicio (obligatorio)
            OutlinedTextField(
                value = serviceName,
                onValueChange = { serviceName = it },
                label = { Text("Nombre del servicio *") },
                placeholder = { Text("Ej: Gmail, Netflix, Banco XYZ") },
                leadingIcon = {
                    Icon(Icons.Default.Business, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Nombre de usuario o correo (opcional)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario o correo") },
                placeholder = { Text("ejemplo@correo.com") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Contraseña (obligatoria)
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña *") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    textStyle = LocalTextStyle.current.copy(fontFamily = MonospaceFontFamily),
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                                )
                            }
                            IconButton(onClick = { showGeneratorDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = "Generar contraseña",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (password.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordStrengthIndicator(strength)
                }
            }

            // Botón para generar contraseña
            Button(
                onClick = { showGeneratorDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Casino, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar contraseña segura")
            }

            // Notas u observaciones (opcional)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas u observaciones") },
                placeholder = { Text("Información adicional...") },
                leadingIcon = {
                    Icon(Icons.Default.Note, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            // Categoría (opcional)
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría") },
                placeholder = { Text("Ej: Redes Sociales, Trabajo, Finanzas") },
                leadingIcon = {
                    Icon(Icons.Default.Label, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val updatedEntry = entry?.copy(
                            serviceName = serviceName,
                            username = username,
                            encryptedPassword = password,
                            notes = notes,
                            category = category
                        ) ?: PasswordEntry(
                            serviceName = serviceName,
                            username = username,
                            encryptedPassword = password,
                            notes = notes,
                            category = category
                        )
                        onSave(updatedEntry)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = serviceName.isNotBlank() && password.isNotBlank()
                ) {
                    Text("Guardar")
                }
            }
        }
    }

    // Diálogo de generador de contraseñas
    if (showGeneratorDialog) {
        PasswordGeneratorDialog(
            onDismiss = { showGeneratorDialog = false },
            onPasswordGenerated = { generatedPassword ->
                password = generatedPassword
                showGeneratorDialog = false
            }
        )
    }
}
