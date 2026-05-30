package com.gestorconecta2.data.repository

import com.gestorconecta2.data.database.AppDatabase
import com.gestorconecta2.data.security.EncryptionManager
import com.gestorconecta2.domain.model.PasswordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio que gestiona las operaciones CRUD de contraseñas.
 * Maneja la encriptación/desencriptación automática de los datos sensibles.
 */
class PasswordRepository(
    private val encryptionManager: EncryptionManager
) {

    private val database: AppDatabase
        get() = AppDatabase.getInstance()

    private val dao = database.passwordDao()

    /**
     * Obtiene todas las contraseñas guardadas.
     * Los campos sensibles se desencriptan antes de devolverlos.
     */
    suspend fun getAllPasswords(): List<PasswordEntry> = withContext(Dispatchers.IO) {
        try {
            val encryptedList = dao.getAllPasswords()
            encryptedList.map { entry ->
                decryptPasswordEntry(entry)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene una contraseña por su ID
     */
    suspend fun getPasswordById(id: Long): PasswordEntry? = withContext(Dispatchers.IO) {
        try {
            val encrypted = dao.getPasswordById(id)
            encrypted?.let { decryptPasswordEntry(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Busca contraseñas por nombre de servicio o usuario
     */
    suspend fun searchPasswords(query: String): List<PasswordEntry> = withContext(Dispatchers.IO) {
        try {
            // La búsqueda se hace sobre los datos encriptados, lo cual es limitado
            // Para una búsqueda mejor, podríamos almacenar hashes de los nombres de servicio
            val encryptedList = dao.searchPasswords("%$query%")
            encryptedList.map { entry ->
                decryptPasswordEntry(entry)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Inserta una nueva contraseña.
     * Los campos sensibles se encriptan antes de guardar.
     * @return El ID del registro insertado
     */
    suspend fun insertPassword(passwordEntry: PasswordEntry): Long = withContext(Dispatchers.IO) {
        val encryptedEntry = encryptPasswordEntry(passwordEntry)
        dao.insertPassword(encryptedEntry)
    }

    /**
     * Actualiza una contraseña existente
     */
    suspend fun updatePassword(passwordEntry: PasswordEntry) = withContext(Dispatchers.IO) {
        val encryptedEntry = encryptPasswordEntry(passwordEntry.copy(updatedAt = System.currentTimeMillis()))
        dao.updatePassword(encryptedEntry)
    }

    /**
     * Elimina una contraseña
     */
    suspend fun deletePassword(passwordEntry: PasswordEntry) = withContext(Dispatchers.IO) {
        // No necesitamos encriptar para eliminar, usamos el ID
        dao.deletePassword(passwordEntry)
    }

    /**
     * Elimina todas las contraseñas
     */
    suspend fun deleteAllPasswords() = withContext(Dispatchers.IO) {
        dao.deleteAllPasswords()
    }

    /**
     * Obtiene el número total de contraseñas guardadas
     */
    suspend fun getPasswordCount(): Int = withContext(Dispatchers.IO) {
        try {
            dao.getPasswordCount()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Encripta los campos sensibles de una entrada
     */
    private fun encryptPasswordEntry(entry: PasswordEntry): PasswordEntry {
        return entry.copy(
            username = encryptionManager.encrypt(entry.username),
            encryptedPassword = encryptionManager.encrypt(entry.encryptedPassword),
            notes = encryptionManager.encrypt(entry.notes)
        )
    }

    /**
     * Desencripta los campos sensibles de una entrada
     */
    private fun decryptPasswordEntry(entry: PasswordEntry): PasswordEntry {
        return try {
            entry.copy(
                username = encryptionManager.decrypt(entry.username),
                encryptedPassword = encryptionManager.decrypt(entry.encryptedPassword),
                notes = encryptionManager.decrypt(entry.notes)
            )
        } catch (e: Exception) {
            // Si falla la desencriptación, devolver valores vacíos
            entry.copy(
                username = "",
                encryptedPassword = "",
                notes = ""
            )
        }
    }

    /**
     * Exporta todas las contraseñas como JSON encriptado.
     * Útil para backup/restore.
     */
    suspend fun exportToEncryptedJson(): String = withContext(Dispatchers.IO) {
        val passwords = getAllPasswords()
        val jsonString = org.json.JSONArray(
            passwords.map { entry ->
                org.json.JSONObject().apply {
                    put("serviceName", entry.serviceName)
                    put("username", entry.username)
                    put("password", entry.encryptedPassword)
                    put("notes", entry.notes)
                    put("category", entry.category)
                    put("createdAt", entry.createdAt)
                }
            }
        ).toString()

        // Encriptar todo el JSON
        encryptionManager.encrypt(jsonString)
    }

    /**
     * Importa contraseñas desde JSON encriptado
     */
    suspend fun importFromEncryptedJson(encryptedJson: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Desencriptar el JSON
            val jsonString = encryptionManager.decrypt(encryptedJson)
            val jsonArray = org.json.JSONArray(jsonString)

            var count = 0
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val entry = PasswordEntry(
                    serviceName = obj.getString("serviceName"),
                    username = obj.optString("username", ""),
                    encryptedPassword = obj.getString("password"),
                    notes = obj.optString("notes", ""),
                    category = obj.optString("category", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = System.currentTimeMillis()
                )
                insertPassword(entry)
                count++
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
