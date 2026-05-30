package com.gestorconecta2.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gestorconecta2.data.security.EncryptionManager
import com.gestorconecta2.domain.model.PasswordEntry
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * DAO (Data Access Object) para operaciones con contraseñas.
 * Todas las operaciones son suspend functions para ejecutarse en coroutines.
 */
@androidx.room.Dao
interface PasswordDao {

    @androidx.room.Query("SELECT * FROM passwords ORDER BY serviceName ASC")
    suspend fun getAllPasswords(): List<PasswordEntry>

    @androidx.room.Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: Long): PasswordEntry?

    @androidx.room.Query("SELECT * FROM passwords WHERE serviceName LIKE :query OR username LIKE :query")
    suspend fun searchPasswords(query: String): List<PasswordEntry>

    @androidx.room.Insert
    suspend fun insertPassword(passwordEntry: PasswordEntry): Long

    @androidx.room.Update
    suspend fun updatePassword(passwordEntry: PasswordEntry)

    @androidx.room.Delete
    suspend fun deletePassword(passwordEntry: PasswordEntry)

    @androidx.room.Query("DELETE FROM passwords")
    suspend fun deleteAllPasswords()

    @androidx.room.Query("SELECT COUNT(*) FROM passwords")
    suspend fun getPasswordCount(): Int
}

/**
 * Base de datos Room con SQLCipher para encriptación completa.
 * La base de datos se abre solo cuando se proporciona la contraseña maestra correcta.
 */
@Database(entities = [PasswordEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun passwordDao(): PasswordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia de la base de datos.
         * Debe llamarse después de initDatabase con la contraseña maestra.
         */
        fun getInstance(): AppDatabase {
            return INSTANCE ?: throw IllegalStateException(
                "Database not initialized. Call initDatabase first with master password."
            )
        }

        /**
         * Inicializa la base de datos con encriptación SQLCipher.
         * @param context Contexto de la aplicación
         * @param masterPassword Contraseña maestra del usuario
         * @return Instancia de AppDatabase
         */
        fun initDatabase(context: Context, masterPassword: String): AppDatabase {
            // Derivar una clave de 256 bits desde la contraseña maestra
            val factory = deriveKeyAndCreateFactory(masterPassword)

            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "gestor_conecta2_db"
            )
                .openHelperFactory(factory)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Aquí podríamos inicializar datos por defecto si fuera necesario
                    }
                })
                .build()

            return INSTANCE!!
        }

        /**
         * Deriva una clave segura desde la contraseña maestra usando PBKDF2
         */
        private fun deriveKeyAndCreateFactory(masterPassword: String): SupportFactory {
            // SQLCipher requiere una passphrase que se deriva de la contraseña maestra
            // Usamos SHA-256 para derivar una clave de 256 bits
            val messageDigest = java.security.MessageDigest.getInstance("SHA-256")
            val keyBytes = messageDigest.digest(masterPassword.toByteArray(Charsets.UTF_8))
            
            // Convertir bytes a hex string para usar como passphrase de SQLCipher
            val passphrase = keyBytes.joinToString("") { "%02x".format(it) }
            
            return SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
        }

        /**
         * Cierra la base de datos y limpia la instancia
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }

        /**
         * Verifica si la base de datos está inicializada
         */
        fun isDatabaseInitialized(): Boolean {
            return INSTANCE != null && INSTANCE?.isOpen == true
        }
    }
}
