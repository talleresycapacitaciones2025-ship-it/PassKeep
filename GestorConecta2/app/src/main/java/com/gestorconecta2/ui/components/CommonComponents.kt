package com.gestorconecta2.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.gestorconecta2.ui.theme.MonospaceFontFamily
import com.gestorconecta2.ui.theme.StrengthMedium
import com.gestorconecta2.ui.theme.StrengthStrong
import com.gestorconecta2.ui.theme.StrengthVeryStrong
import com.gestorconecta2.ui.theme.StrengthWeak
import com.gestorconecta2.util.PasswordStrength
import kotlinx.coroutines.delay

/**
 * Componente para mostrar un campo de contraseña con opción de mostrar/ocultar.
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Contraseña",
    readOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        readOnly = readOnly,
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        }
    )
}

/**
 * Indicador visual de fortaleza de contraseña.
 */
@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val color = when (strength) {
        PasswordStrength.WEAK -> StrengthWeak
        PasswordStrength.MEDIUM -> StrengthMedium
        PasswordStrength.STRONG -> StrengthStrong
        PasswordStrength.VERY_STRONG -> StrengthVeryStrong
    }

    val label = when (strength) {
        PasswordStrength.WEAK -> "Débil"
        PasswordStrength.MEDIUM -> "Media"
        PasswordStrength.STRONG -> "Fuerte"
        PasswordStrength.VERY_STRONG -> "Muy Fuerte"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            if (index < when (strength) {
                                    PasswordStrength.WEAK -> 1
                                    PasswordStrength.MEDIUM -> 2
                                    PasswordStrength.STRONG -> 3
                                    PasswordStrength.VERY_STRONG -> 4
                                }) color else Color.Gray.copy(alpha = 0.3f)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

/**
 * Generador de contraseñas con efecto "tragamonedas".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorDialog(
    onDismiss: () -> Unit,
    onPasswordGenerated: (String) -> Unit,
    initialLength: Int = 12,
    initialIncludeUppercase: Boolean = true,
    initialIncludeNumbers: Boolean = true,
    initialIncludeSymbols: Boolean = true
) {
    var passwordLength by remember { mutableStateOf(initialLength) }
    var includeUppercase by remember { mutableStateOf(initialIncludeUppercase) }
    var includeNumbers by remember { mutableStateOf(initialIncludeNumbers) }
    var includeSymbols by remember { mutableStateOf(initialIncludeSymbols) }
    var generatedPassword by remember { mutableStateOf("") }
    var isAnimating by remember { mutableStateOf(false) }

    // Animación tipo tragamonedas
    LaunchedEffect(generatedPassword) {
        if (isAnimating) {
            repeat(20) { iteration ->
                generatedPassword = generateRandomTemp(passwordLength)
                delay(timeMillis = (50 + iteration * 5).toLong())
            }
            isAnimating = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generar Contraseña Segura") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Campo de contraseña generada
                OutlinedTextField(
                    value = generatedPassword,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Contraseña Generada") },
                    textStyle = LocalTextStyle.current.copy(fontFamily = MonospaceFontFamily),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { onPasswordGenerated(generatedPassword); onDismiss() }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ContentCopy,
                                contentDescription = "Copiar y usar"
                            )
                        }
                    }
                )

                // Control deslizante de longitud
                Column {
                    Text("Longitud: $passwordLength caracteres")
                    Slider(
                        value = passwordLength.toFloat(),
                        onValueChange = { passwordLength = it.toInt() },
                        valueRange = 8f..20f,
                        steps = 11
                    )
                }

                // Opciones
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = includeUppercase,
                        onClick = { includeUppercase = !includeUppercase },
                        label = { Text("Mayúsculas") },
                        leadingIcon = if (includeUppercase) {
                            {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )

                    FilterChip(
                        selected = includeNumbers,
                        onClick = { includeNumbers = !includeNumbers },
                        label = { Text("Números") },
                        leadingIcon = if (includeNumbers) {
                            {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )

                    FilterChip(
                        selected = includeSymbols,
                        onClick = { includeSymbols = !includeSymbols },
                        label = { Text("Símbolos") },
                        leadingIcon = if (includeSymbols) {
                            {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isAnimating = true
                },
                enabled = !isAnimating
            ) {
                Text("Generar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Genera una contraseña temporal aleatoria para la animación
 */
private fun generateRandomTemp(length: Int): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789¡@#$%^&*()"
    return List(length) { chars.random() }.joinToString("")
}
