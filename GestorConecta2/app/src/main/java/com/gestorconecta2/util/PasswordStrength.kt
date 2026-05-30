package com.gestorconecta2.util

/**
 * Enumeración que representa los niveles de fortaleza de una contraseña.
 */
enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}

/**
 * Utilidad para evaluar la fortaleza de una contraseña.
 */
object PasswordStrengthEvaluator {

    /**
     * Evalúa la fortaleza de una contraseña y devuelve el nivel correspondiente.
     * 
     * Criterios:
     * - Débil: menos de 8 caracteres o solo letras minúsculas
     * - Media: 8+ caracteres con mayúsculas o números
     * - Fuerte: 10+ caracteres con mayúsculas, números y símbolos
     * - Muy Fuerte: 12+ caracteres con todos los tipos de caracteres
     */
    fun evaluate(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.WEAK
        
        var score = 0
        
        // Longitud
        when {
            password.length >= 12 -> score += 3
            password.length >= 10 -> score += 2
            password.length >= 8 -> score += 1
            else -> return PasswordStrength.WEAK
        }
        
        // Mayúsculas
        if (password.any { it.isUpperCase() }) score += 1
        
        // Números
        if (password.any { it.isDigit() }) score += 1
        
        // Símbolos
        if (password.any { !it.isLetterOrDigit() }) score += 1
        
        // Determinar nivel
        return when {
            score >= 6 -> PasswordStrength.VERY_STRONG
            score >= 5 -> PasswordStrength.STRONG
            score >= 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
}
