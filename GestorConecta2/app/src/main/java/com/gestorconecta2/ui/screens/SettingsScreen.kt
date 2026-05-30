package com.gestorconecta2.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gestorconecta2.data.security.SecurePreferences
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import org.json.JSONObject

/**
 * Pantalla de ajustes y configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLockVault: () -> Unit,
    onResetAllData: () -> Unit,
    darkModeEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onBack: () -> Unit,
    appVersion: String = "1.0.0",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showExportConfirmDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Launcher para exportar archivo
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            // Aquí se manejaría la escritura del archivo encriptado
            // Por simplicidad, mostramos un mensaje
        }
    }

    // Launcher para importar archivo
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Aquí se manejaría la lectura del archivo encriptado
            // Por simplicidad, mostramos un mensaje
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Sección: Seguridad
            Text(
                text = "Seguridad",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Cambiar contraseña maestra") },
                        leadingContent = {
                            Icon(Icons.Default.LockReset, contentDescription = null)
                        },
                        modifier = Modifier.clickable { showChangePasswordDialog = true }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Cerrar sesión") },
                        supportingContent = { Text("Bloquear la bóveda de contraseñas") },
                        leadingContent = {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                        },
                        modifier = Modifier.clickable { onLockVault() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Copia de seguridad
            Text(
                text = "Copia de seguridad",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Exportar contraseñas") },
                        supportingContent = { Text("Guardar copia encriptada en Descargas") },
                        leadingContent = {
                            Icon(Icons.Default.Upload, contentDescription = null)
                        },
                        modifier = Modifier.clickable { showExportConfirmDialog = true }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Importar contraseñas") },
                        supportingContent = { Text("Restaurar desde archivo encriptado") },
                        leadingContent = {
                            Icon(Icons.Default.Download, contentDescription = null)
                        },
                        modifier = Modifier.clickable { importLauncher.launch("application/json") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Apariencia
            Text(
                text = "Apariencia",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = onToggleDarkMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )
                Text("Modo oscuro")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Datos
            Text(
                text = "Datos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            "Borrar todos los datos",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        ) 
                    },
                    supportingContent = { 
                        Text(
                            "Eliminar todas las contraseñas y restablecer la app",
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        ) 
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    },
                    modifier = Modifier.clickable { showResetConfirmDialog = true }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Versión de la app
            Text(
                text = "Versión $appVersion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }
    }

    // Diálogo de confirmación para exportar
    if (showExportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExportConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Exportar contraseñas") },
            text = {
                Text(
                    "Se creará un archivo encriptado con todas tus contraseñas. " +
                    "GUÁRDALO EN UN LUGAR SEGURO. Si alguien obtiene este archivo y tu contraseña maestra, " +
                    "podrá acceder a todas tus contraseñas."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        exportLauncher.launch("gestor_conecta2_backup_${System.currentTimeMillis()}.json")
                        showExportConfirmDialog = false
                    }
                ) {
                    Text("Exportar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para reset
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("¿Estás seguro?") },
            text = {
                Text(
                    "Esta acción BORRARÁ TODAS las contraseñas guardadas de forma PERMANENTE. " +
                    "No hay forma de recuperarlas. ¿Continuar?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Borrar todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para cambiar contraseña maestra
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onPasswordChanged = {
                showChangePasswordDialog = false
            }
        )
    }
}

/**
 * Diálogo para cambiar la contraseña maestra
 */
@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onPasswordChanged: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña maestra") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = confirmPassword.isNotBlank() && newPassword != confirmPassword
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        currentPassword.isBlank() -> error = "Ingresa tu contraseña actual"
                        newPassword.length < 6 -> error = "La nueva contraseña debe tener al menos 6 caracteres"
                        newPassword != confirmPassword -> error = "Las contraseñas no coinciden"
                        else -> {
                            // Aquí se llamaría al ViewModel para cambiar la contraseña
                            onPasswordChanged()
                        }
                    }
                }
            ) {
                Text("Cambiar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
