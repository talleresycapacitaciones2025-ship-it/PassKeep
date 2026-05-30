package com.gestorconecta2.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modelo de datos para una contraseña guardada.
 * Se almacena encriptada en la base de datos.
 */
@Entity(tableName = "passwords")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Nombre del servicio o sitio web (ej: "Gmail", "Netflix")
    val serviceName: String,
    
    // Nombre de usuario o correo asociado (encriptado)
    val username: String = "",
    
    // Contraseña encriptada
    val encryptedPassword: String,
    
    // Notas u observaciones adicionales (encriptado)
    val notes: String = "",
    
    // Fecha de creación en timestamp
    val createdAt: Long = System.currentTimeMillis(),
    
    // Fecha de última modificación
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Categoría o etiqueta opcional
    val category: String = ""
) {
    /**
     * Verifica si la entrada tiene datos válidos
     */
    fun isValid(): Boolean {
        return serviceName.isNotBlank() && encryptedPassword.isNotBlank()
    }
}
