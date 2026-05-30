package com.gestorconecta2

import android.app.Application

/**
 * Clase de aplicación para inicializar componentes globales.
 */
class GestorConecta2App : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar SQLCipher (requiere llamar a esto antes de usar la base de datos)
        try {
            net.sqlcipher.database.SQLiteDatabase.loadLibs(this)
        } catch (e: Exception) {
            // Manejar error de carga de librerías SQLCipher
            e.printStackTrace()
        }
    }
}
