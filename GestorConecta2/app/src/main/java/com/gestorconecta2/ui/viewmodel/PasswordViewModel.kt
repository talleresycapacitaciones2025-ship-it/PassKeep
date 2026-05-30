package com.gestorconecta2.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestorconecta2.data.database.AppDatabase
import com.gestorconecta2.data.repository.PasswordRepository
import com.gestorconecta2.data.security.EncryptionManager
import com.gestorconecta2.data.security.SecurePreferences
import com.gestorconecta2.domain.model.PasswordEntry
import com.gestorconecta2.util.PasswordGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel principal para la gestión de contraseñas.
 * Maneja el estado de autenticación, CRUD de contraseñas y preferencias.
 */
class PasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val encryptionManager = EncryptionManager()
    private val securePreferences = SecurePreferences(application)
    private lateinit var passwordRepository: PasswordRepository
    private val passwordGenerator = PasswordGenerator()

    // Estado de autenticación
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotInitialized)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Lista de contraseñas
    private val _passwordList = MutableStateFlow<List<PasswordEntry>>(emptyList())
    val passwordList: StateFlow<List<PasswordEntry>> = _passwordList.asStateFlow()

    // Contraseña seleccionada para ver/editar
    private val _selectedPassword = MutableStateFlow<PasswordEntry?>(null)
    val selectedPassword: StateFlow<PasswordEntry?> = _selectedPassword.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensajes de error/éxito
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Preferencias
    private val _darkModeEnabled = MutableStateFlow(securePreferences.getDarkMode())
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    init {
        checkAuthState()
    }

    /**
     * Verifica el estado de autenticación al iniciar
     */
    private fun checkAuthState() {
        if (!securePreferences.checkInitialization()) {
            _authState.value = AuthState.NotInitialized
        } else {
            _authState.value = AuthState.Locked
        }
    }

    /**
     * Establece la contraseña maestra por primera vez
     */
    fun setMasterPassword(password: String): Boolean {
        if (password.length < 6) {
            _uiMessage.value = "La contraseña maestra debe tener al menos 6 caracteres"
            return false
        }

        securePreferences.setMasterPasswordHash(password)
        
        // Inicializar base de datos con la nueva contraseña
        try {
            AppDatabase.initDatabase(getApplication(), password)
            passwordRepository = PasswordRepository(encryptionManager)
            _authState.value = AuthState.Unlocked
            _uiMessage.value = "Contraseña maestra establecida correctamente"
            return true
        } catch (e: Exception) {
            _uiMessage.value = "Error al inicializar: ${e.message}"
            return false
        }
    }

    /**
     * Intenta desbloquear la bóveda con la contraseña proporcionada
     */
    fun unlockVault(password: String): Boolean {
        if (!securePreferences.verifyMasterPassword(password)) {
            _uiMessage.value = "Contraseña incorrecta"
            return false
        }

        try {
            AppDatabase.initDatabase(getApplication(), password)
            passwordRepository = PasswordRepository(encryptionManager)
            _authState.value = AuthState.Unlocked
            loadPasswords()
            _uiMessage.value = "Bóveda desbloqueada"
            return true
        } catch (e: Exception) {
            _uiMessage.value = "Error al desbloquear: ${e.message}"
            return false
        }
    }

    /**
     * Bloquea la bóveda (cierra sesión)
     */
    fun lockVault() {
        AppDatabase.closeDatabase()
        _authState.value = AuthState.Locked
        _passwordList.value = emptyList()
        _selectedPassword.value = null
        _uiMessage.value = "Sesión cerrada"
    }

    /**
     * Carga todas las contraseñas desde la base de datos
     */
    fun loadPasswords() {
        if (_authState.value != AuthState.Unlocked) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _passwordList.value = passwordRepository.getAllPasswords()
            } catch (e: Exception) {
                _uiMessage.value = "Error al cargar contraseñas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Busca contraseñas por texto
     */
    fun searchPasswords(query: String) {
        if (_authState.value != AuthState.Unlocked) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (query.isBlank()) {
                    _passwordList.value = passwordRepository.getAllPasswords()
                } else {
                    _passwordList.value = passwordRepository.searchPasswords(query)
                }
            } catch (e: Exception) {
                _uiMessage.value = "Error en búsqueda: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Guarda una nueva contraseña o actualiza una existente
     */
    fun savePassword(entry: PasswordEntry) {
        if (_authState.value != AuthState.Unlocked) return

        if (!entry.isValid()) {
            _uiMessage.value = "El nombre del servicio y la contraseña son obligatorios"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (entry.id == 0L) {
                    // Nueva entrada
                    val newId = passwordRepository.insertPassword(entry)
                    _uiMessage.value = "Contraseña guardada"
                } else {
                    // Actualizar entrada existente
                    passwordRepository.updatePassword(entry)
                    _uiMessage.value = "Contraseña actualizada"
                }
                loadPasswords()
                _selectedPassword.value = null
            } catch (e: Exception) {
                _uiMessage.value = "Error al guardar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina una contraseña
     */
    fun deletePassword(entry: PasswordEntry) {
        if (_authState.value != AuthState.Unlocked) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                passwordRepository.deletePassword(entry)
                _uiMessage.value = "Contraseña eliminada"
                loadPasswords()
                _selectedPassword.value = null
            } catch (e: Exception) {
                _uiMessage.value = "Error al eliminar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Selecciona una contraseña para ver/editar
     */
    fun selectPassword(entry: PasswordEntry) {
        _selectedPassword.value = entry
    }

    /**
     * Deselecciona la contraseña actual
     */
    fun clearSelection() {
        _selectedPassword.value = null
    }

    /**
     * Genera una contraseña segura
     */
    fun generateSecurePassword(
        length: Int = 12,
        includeUppercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        return passwordGenerator.generatePassword(length, includeUppercase, includeNumbers, includeSymbols)
    }

    /**
     * Calcula la fortaleza de una contraseña
     */
    fun getPasswordStrength(password: String) = passwordGenerator.calculateStrength(password)

    /**
     * Cambia la contraseña maestra
     */
    fun changeMasterPassword(current: String, new: String): Boolean {
        if (new.length < 6) {
            _uiMessage.value = "La nueva contraseña debe tener al menos 6 caracteres"
            return false
        }

        return if (securePreferences.changeMasterPassword(current, new)) {
            // Re-inicializar la base de datos con la nueva contraseña
            AppDatabase.closeDatabase()
            AppDatabase.initDatabase(getApplication(), new)
            _uiMessage.value = "Contraseña maestra cambiada"
            true
        } else {
            _uiMessage.value = "Contraseña actual incorrecta"
            false
        }
    }

    /**
     * Exporta las contraseñas a JSON encriptado
     */
    suspend fun exportPasswords(): Result<String> {
        return try {
            val encryptedJson = passwordRepository.exportToEncryptedJson()
            Result.success(encryptedJson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Importa contraseñas desde JSON encriptado
     */
    suspend fun importPasswords(encryptedJson: String): Result<Int> {
        return passwordRepository.importFromEncryptedJson(encryptedJson)
    }

    /**
     * Borra todos los datos (reset completo)
     */
    fun resetAllData() {
        viewModelScope.launch {
            try {
                passwordRepository.deleteAllPasswords()
                securePreferences.clearAll()
                encryptionManager.deleteMasterKey()
                AppDatabase.closeDatabase()
                _authState.value = AuthState.NotInitialized
                _passwordList.value = emptyList()
                _uiMessage.value = "Datos borrados correctamente"
            } catch (e: Exception) {
                _uiMessage.value = "Error al borrar datos: ${e.message}"
            }
        }
    }

    /**
     * Activa/desactiva modo oscuro
     */
    fun toggleDarkMode(enabled: Boolean) {
        securePreferences.setDarkMode(enabled)
        _darkModeEnabled.value = enabled
    }

    /**
     * Limpia el mensaje UI después de mostrarlo
     */
    fun clearUiMessage() {
        _uiMessage.value = null
    }
}

/**
 * Estados posibles de autenticación
 */
sealed class AuthState {
    object NotInitialized : AuthState() // Primera vez, debe crear contraseña maestra
    object Locked : AuthState() // Debe ingresar contraseña maestra
    object Unlocked : AuthState() // Autenticado, puede acceder a las contraseñas
}
