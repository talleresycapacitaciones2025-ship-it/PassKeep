package com.gestorconecta2.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor de preferencias encriptadas para almacenar:
 * - Estado de inicialización de la contraseña maestra
 * - Hash verificador de la contraseña maestra
 * - Configuraciones de la aplicación
 */
class SecurePreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_prefs"
        const val KEY_MASTER_PASSWORD_HASH = "master_password_hash"
        const val KEY_IS_INITIALIZED = "is_initialized"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_AUTOLOCK_TIME = "autolock_time"
        const val KEY_CLIPBOARD_CLEAR_TIME = "clipboard_clear_time"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _isInitialized = MutableStateFlow(encryptedPrefs.getBoolean(KEY_IS_INITIALIZED, false))
    val isInitialized: Flow<Boolean> = _isInitialized.asStateFlow()

    /**
     * Verifica si la contraseña maestra ya fue establecida
     */
    fun checkInitialization(): Boolean {
        val initialized = encryptedPrefs.getBoolean(KEY_IS_INITIALIZED, false)
        _isInitialized.value = initialized
        return initialized
    }

    /**
     * Guarda el hash de la contraseña maestra (NUNCA guardar la contraseña en texto plano)
     * Usamos SHA-256 con salt para mayor seguridad
     */
    fun setMasterPasswordHash(password: String) {
        val salt = java.util.UUID.randomUUID().toString()
        val hash = hashPassword(password, salt)
        
        encryptedPrefs.edit().apply {
            putString(KEY_MASTER_PASSWORD_HASH, "$salt:$hash")
            putBoolean(KEY_IS_INITIALIZED, true)
            apply()
        }
        _isInitialized.value = true
    }

    /**
     * Verifica si la contraseña proporcionada es correcta
     */
    fun verifyMasterPassword(password: String): Boolean {
        val storedData = encryptedPrefs.getString(KEY_MASTER_PASSWORD_HASH, null) ?: return false
        
        val parts = storedData.split(":")
        if (parts.size != 2) return false
        
        val salt = parts[0]
        val storedHash = parts[1]
        val inputHash = hashPassword(password, salt)
        
        return storedHash == inputHash
    }

    /**
     * Genera un hash seguro de la contraseña usando SHA-256
     */
    private fun hashPassword(password: String, salt: String): String {
        val saltedPassword = password + salt
        val bytes = saltedPassword.toByteArray(Charsets.UTF_8)
        val messageDigest = java.security.MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Cambia la contraseña maestra (requiere verificar la actual primero)
     */
    fun changeMasterPassword(currentPassword: String, newPassword: String): Boolean {
        if (!verifyMasterPassword(currentPassword)) {
            return false
        }
        
        if (newPassword.length < 6) {
            return false
        }
        
        setMasterPasswordHash(newPassword)
        return true
    }

    /**
     * Guarda preferencia de modo oscuro
     */
    fun setDarkMode(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    /**
     * Obtiene preferencia de modo oscuro
     */
    fun getDarkMode(): Boolean {
        return encryptedPrefs.getBoolean(KEY_DARK_MODE, false)
    }

    /**
     * Guarda tiempo de autobloqueo en minutos
     */
    fun setAutoLockTime(minutes: Int) {
        encryptedPrefs.edit().putInt(KEY_AUTOLOCK_TIME, minutes).apply()
    }

    /**
     * Obtiene tiempo de autobloqueo
     */
    fun getAutoLockTime(): Int {
        return encryptedPrefs.getInt(KEY_AUTOLOCK_TIME, 5)
    }

    /**
     * Guarda tiempo para limpiar portapapeles en segundos
     */
    fun setClipboardClearTime(seconds: Int) {
        encryptedPrefs.edit().putInt(KEY_CLIPBOARD_CLEAR_TIME, seconds).apply()
    }

    /**
     * Obtiene tiempo para limpiar portapapeles
     */
    fun getClipboardClearTime(): Int {
        return encryptedPrefs.getInt(KEY_CLIPBOARD_CLEAR_TIME, 30)
    }

    /**
     * Borra todas las preferencias (solo para reseteo completo)
     */
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
        _isInitialized.value = false
    }
}
