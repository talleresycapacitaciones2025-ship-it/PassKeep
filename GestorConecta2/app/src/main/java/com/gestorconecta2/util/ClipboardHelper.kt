package com.gestorconecta2.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Utilidad para manejar el portapapeles de forma segura.
 * Limpia automáticamente la contraseña después de un tiempo determinado.
 */
class ClipboardHelper(private val context: Context) {

    private val clipboardManager: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    /**
     * Copia texto al portapapeles y programa su eliminación automática.
     * @param text Texto a copiar
     * @param label Etiqueta descriptiva
     * @param clearAfterMs Tiempo en milisegundos para limpiar (default 30 segundos)
     * @param onCleared Callback opcional cuando se limpia el portapapeles
     */
    fun copyToClipboard(
        text: String,
        label: String = "Contraseña",
        clearAfterMs: Long = 30000L,
        onCleared: (() -> Unit)? = null
    ) {
        // Crear clip con el texto
        val clip = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clip)

        // Mostrar confirmación
        Toast.makeText(context, "$label copiada", Toast.LENGTH_SHORT).show()

        // Programar limpieza automática
        CoroutineScope(Dispatchers.Main).launch {
            delay(clearAfterMs)
            clearClipboard(onCleared)
        }
    }

    /**
     * Limpia el portapapeles inmediatamente
     */
    fun clearClipboard(onCleared: (() -> Unit)? = null) {
        clipboardManager.clearPrimaryClip()
        onCleared?.invoke()
    }

    /**
     * Obtiene el texto actual del portapapeles
     */
    fun getClipboardText(): String? {
        return if (clipboardManager.hasPrimaryClip()) {
            clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
        } else {
            null
        }
    }

    /**
     * Verifica si hay texto en el portapapeles
     */
    fun hasClipboardText(): Boolean {
        return clipboardManager.hasPrimaryClip()
    }
}

/**
 * Extensión para copiar texto fácilmente desde cualquier Context
 */
fun Context.copyToClipboardSecure(
    text: String,
    label: String = "Contraseña",
    clearAfterMs: Long = 30000L
) {
    val helper = ClipboardHelper(this)
    helper.copyToClipboard(text, label, clearAfterMs)
}
