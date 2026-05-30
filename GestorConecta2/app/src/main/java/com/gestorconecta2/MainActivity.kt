package com.gestorconecta2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.gestorconecta2.ui.screens.*
import com.gestorconecta2.ui.theme.GestorConecta2Theme
import com.gestorconecta2.ui.viewmodel.AuthState
import com.gestorconecta2.ui.viewmodel.PasswordViewModel
import com.gestorconecta2.util.ClipboardHelper
import kotlinx.coroutines.launch

/**
 * Actividad principal de la aplicación.
 * Maneja la navegación entre pantallas según el estado de autenticación.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: PasswordViewModel by viewModels()
    private lateinit var clipboardHelper: ClipboardHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        clipboardHelper = ClipboardHelper(this)

        setContent {
            GestorConecta2Theme(
                darkTheme = viewModel.darkModeEnabled.collectAsState().value
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        viewModel = viewModel,
                        clipboardHelper = clipboardHelper
                    )
                }
            }
        }

        // Observar mensajes UI para mostrar Snackbars
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiMessage.collect { message ->
                    message?.let {
                        // Aquí se podría mostrar un Snackbar
                        viewModel.clearUiMessage()
                    }
                }
            }
        }
    }
}

/**
 * Navegación principal basada en el estado de autenticación.
 */
@Composable
fun MainNavigation(
    viewModel: PasswordViewModel,
    clipboardHelper: ClipboardHelper
) {
    val authState by viewModel.authState.collectAsState()
    val passwords by viewModel.passwordList.collectAsState()
    val selectedPassword by viewModel.selectedPassword.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var showEditScreen by remember { mutableStateOf(false) }
    var showDetailScreen by remember { mutableStateOf(false) }

    when (authState) {
        is AuthState.NotInitialized -> {
            // Primera vez: establecer contraseña maestra
            SetMasterPasswordScreen(
                onPasswordSet = { password ->
                    viewModel.setMasterPassword(password)
                }
            )
        }

        is AuthState.Locked -> {
            // Debe desbloquear con contraseña maestra
            UnlockVaultScreen(
                onUnlock = { password ->
                    viewModel.unlockVault(password)
                }
            )
        }

        is AuthState.Unlocked -> {
            // Autenticado: mostrar pantallas principales
            when {
                showSettings -> {
                    SettingsScreen(
                        onLockVault = {
                            viewModel.lockVault()
                            showSettings = false
                        },
                        onResetAllData = {
                            viewModel.resetAllData()
                            showSettings = false
                        },
                        darkModeEnabled = viewModel.darkModeEnabled.collectAsState().value,
                        onToggleDarkMode = { enabled ->
                            viewModel.toggleDarkMode(enabled)
                        },
                        onBack = { showSettings = false }
                    )
                }

                showEditScreen -> {
                    PasswordEditScreen(
                        entry = selectedPassword,
                        onSave = { entry ->
                            viewModel.savePassword(entry)
                            showEditScreen = false
                        },
                        onCancel = {
                            viewModel.clearSelection()
                            showEditScreen = false
                        },
                        onGeneratePassword = {
                            viewModel.generateSecurePassword()
                        },
                        getPasswordStrength = { password ->
                            viewModel.getPasswordStrength(password)
                        }
                    )
                }

                showDetailScreen && selectedPassword != null -> {
                    PasswordDetailScreen(
                        entry = selectedPassword!!,
                        onCopyPassword = {
                            clipboardHelper.copyToClipboard(
                                selectedPassword!!.encryptedPassword,
                                "Contraseña"
                            )
                        },
                        onCopyUsername = {
                            clipboardHelper.copyToClipboard(
                                selectedPassword!!.username,
                                "Usuario"
                            )
                        },
                        onEdit = {
                            showDetailScreen = false
                            showEditScreen = true
                        },
                        onDelete = {
                            viewModel.deletePassword(selectedPassword!!)
                            showDetailScreen = false
                        },
                        onBack = {
                            viewModel.clearSelection()
                            showDetailScreen = false
                        }
                    )
                }

                else -> {
                    PasswordListScreen(
                        passwords = passwords,
                        isLoading = isLoading,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { query ->
                            searchQuery = query
                            viewModel.searchPasswords(query)
                        },
                        onPasswordClick = { entry ->
                            viewModel.selectPassword(entry)
                            showDetailScreen = true
                        },
                        onCopyPassword = { entry ->
                            clipboardHelper.copyToClipboard(
                                entry.encryptedPassword,
                                "Contraseña"
                            )
                        },
                        onAddNewPassword = {
                            viewModel.clearSelection()
                            showEditScreen = true
                        },
                        onNavigateToSettings = {
                            showSettings = true
                        }
                    )
                }
            }
        }
    }
}
