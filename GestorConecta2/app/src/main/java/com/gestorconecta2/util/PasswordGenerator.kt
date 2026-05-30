package com.gestorconecta2.util

/**
 * Generador de contraseñas seguras.
 * Similar al generador de Conecta2 con efecto "tragamonedas".
 */
class PasswordGenerator {

    companion object {
        private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
        private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val NUMBERS = "0123456789"
        private const val SYMBOLS = "¡@#$%^&*()_+-=[]{}|;:,.<>?"
    }

    /**
     * Genera una contraseña aleatoria segura.
     * @param length Longitud de la contraseña (8-20)
     * @param includeUppercase Incluir mayúsculas
     * @param includeNumbers Incluir números
     * @param includeSymbols Incluir símbolos
     * @return Contraseña generada
     */
    fun generatePassword(
        length: Int = 12,
        includeUppercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        require(length in 8..20) { "La longitud debe estar entre 8 y 20 caracteres" }

        var charSet = LOWERCASE
        if (includeUppercase) charSet += UPPERCASE
        if (includeNumbers) charSet += NUMBERS
        if (includeSymbols) charSet += SYMBOLS

        // Asegurar al menos un carácter de cada tipo seleccionado
        val requiredChars = mutableListOf<Char>()
        if (includeUppercase) requiredChars.add(UPPERCASE.random())
        if (includeNumbers) requiredChars.add(NUMBERS.random())
        if (includeSymbols) requiredChars.add(SYMBOLS.random())

        // Rellenar el resto aleatoriamente
        val randomChars = List(length - requiredChars.size) { charSet.random() }
        
        // Combinar y mezclar
        val allChars = (requiredChars + randomChars).shuffled()
        
        return allChars.joinToString("")
    }

    /**
     * Calcula la fortaleza de una contraseña.
     * @return Valor entre 0.0 (débil) y 1.0 (muy fuerte)
     */
    fun calculateStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.WEAK

        var score = 0.0

        // Longitud
        score += when {
            password.length >= 16 -> 0.4
            password.length >= 12 -> 0.3
            password.length >= 8 -> 0.2
            else -> 0.1
        }

        // Variedad de caracteres
        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        val varietyCount = listOf(hasLower, hasUpper, hasDigit, hasSymbol).count { it }
        score += when (varietyCount) {
            4 -> 0.4
            3 -> 0.3
            2 -> 0.2
            1 -> 0.1
            else -> 0.0
        }

        // Bonus por no tener patrones obvios
        if (!hasSequentialChars(password) && !hasRepeatedChars(password)) {
            score += 0.2
        }

        return when {
            score >= 0.8 -> PasswordStrength.VERY_STRONG
            score >= 0.6 -> PasswordStrength.STRONG
            score >= 0.4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }

    /**
     * Verifica si la contraseña tiene caracteres secuenciales (abc, 123, etc.)
     */
    private fun hasSequentialChars(password: String): Boolean {
        val lowerPwd = password.lowercase()
        for (i in 0 until lowerPwd.length - 2) {
            val c1 = lowerPwd[i].code
            val c2 = lowerPwd[i + 1].code
            val c3 = lowerPwd[i + 2].code
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true
            }
        }
        return false
    }

    /**
     * Verifica si la contraseña tiene caracteres repetidos consecutivos
     */
    private fun hasRepeatedChars(password: String): Boolean {
        for (i in 0 until password.length - 2) {
            if (password[i] == password[i + 1] && password[i + 1] == password[i + 2]) {
                return true
            }
        }
        return false
    }

    /**
     * Genera una contraseña temporal para el efecto "tragamonedas"
     */
    fun generateRandomTempPassword(length: Int = 12): String {
        val chars = LOWERCASE + UPPERCASE + NUMBERS + SYMBOLS
        return List(length) { chars.random() }.joinToString("")
    }
}

/**
 * Enum que representa los niveles de fortaleza de contraseña
 */
enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}
