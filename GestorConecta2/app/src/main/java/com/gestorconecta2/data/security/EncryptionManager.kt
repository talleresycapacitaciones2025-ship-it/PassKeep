package com.gestorconecta2.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gestor de encriptación usando Android Keystore.
 * Proporciona encriptación AES-256-GCM para máxima seguridad.
 */
class EncryptionManager {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_ALIAS = "GestorConecta2MasterKey"
        private const val IV_SIZE = 12 // bytes for GCM
        private const val TAG_SIZE_BIT_LENGTH = 128
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    init {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateMasterKey()
        }
    }

    /**
     * Genera una clave maestra segura en el Keystore de Android
     */
    private fun generateMasterKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(parameterSpec)
        keyGenerator.generateKey()
    }

    /**
     * Obtiene la clave secreta del Keystore
     */
    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    /**
     * Encripta un texto plano usando AES-256-GCM
     * @param plainText El texto a encriptar
     * @return String con formato: IV (base64):CIPHERTEXT (base64)
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val ivBase64 = android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP)
        val encryptedBase64 = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.NO_WRAP)

        return "$ivBase64:$encryptedBase64"
    }

    /**
     * Desencripta un texto encriptado
     * @param encryptedData String con formato IV:CIPHERTEXT
     * @return El texto plano original
     */
    fun decrypt(encryptedData: String): String {
        val parts = encryptedData.split(":")
        require(parts.size == 2) { "Formato de datos encriptados inválido" }

        val iv = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP)
        val encryptedBytes = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_SIZE_BIT_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Encripta múltiples campos y devuelve un mapa encriptado
     */
    fun encryptFields(vararg fields: Pair<String, String>): Map<String, String> {
        return fields.associate { (key, value) ->
            key to encrypt(value)
        }
    }

    /**
     * Desencripta múltiples campos desde un mapa encriptado
     */
    fun decryptFields(encryptedMap: Map<String, String>): Map<String, String> {
        return encryptedMap.mapValues { (_, value) ->
            decrypt(value)
        }
    }

    /**
     * Verifica si el keystore está disponible y seguro
     */
    fun isKeystoreSecure(): Boolean {
        return try {
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Elimina la clave maestra (solo para reseteo completo)
     */
    fun deleteMasterKey() {
        try {
            keyStore.deleteEntry(KEY_ALIAS)
        } catch (e: Exception) {
            // Ignorar errores al eliminar
        }
    }
}
