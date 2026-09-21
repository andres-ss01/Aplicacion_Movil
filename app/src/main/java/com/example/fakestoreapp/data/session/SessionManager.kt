package com.example.fakestoreapp.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.fakestoreapp.data.model.UserRole

/**
 * Maneja la persistencia local de la sesión (token, datos del usuario y rol) usando
 * EncryptedSharedPreferences, el mecanismo de almacenamiento seguro nativo recomendado
 * en Android (regla de negocio de US01).
 *
 * IMPORTANTE (US02): clear() borra TODO el contenido del almacenamiento persistente,
 * no solo variables en memoria, cumpliendo la "limpieza profunda" exigida.
 */
class SessionManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            createEncryptedSharedPreferences()
        } catch (e: Exception) {
            // Manejo de excepción javax.crypto.AEADBadTagException o corrupción de claves en KeyStore
            context.deleteSharedPreferences(PREFS_FILE_NAME)
            createEncryptedSharedPreferences()
        }
    }

    private fun createEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Guarda los datos completos de la sesión del usuario tras un login exitoso.
     */
    fun saveSession(
        token: String,
        userId: Int,
        username: String,
        role: UserRole,
        email: String? = null,
        phone: String? = null,
        address: String? = null
    ) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_ROLE, role.name)

            if (email != null) putString(KEY_EMAIL, email) else remove(KEY_EMAIL)
            if (phone != null) putString(KEY_PHONE, phone) else remove(KEY_PHONE)
            if (address != null) putString(KEY_ADDRESS, address) else remove(KEY_ADDRESS)

            apply()
        }
    }

    /**
     * Permite guardar o actualizar únicamente la información extendida del perfil de usuario
     * (por ejemplo, tras consultar la API /users/{id}).
     */
    fun saveUserDetails(email: String?, phone: String?, address: String?) {
        prefs.edit().apply {
            if (email != null) putString(KEY_EMAIL, email) else remove(KEY_EMAIL)
            if (phone != null) putString(KEY_PHONE, phone) else remove(KEY_PHONE)
            if (address != null) putString(KEY_ADDRESS, address) else remove(KEY_ADDRESS)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)

    fun getAddress(): String? = prefs.getString(KEY_ADDRESS, null)

    fun getRole(): UserRole {
        val stored = prefs.getString(KEY_ROLE, null) ?: return UserRole.CLIENTE
        return try {
            UserRole.valueOf(stored)
        } catch (e: IllegalArgumentException) {
            UserRole.CLIENTE
        }
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    /**
     * Limpieza profunda de credenciales (US02 - Escenario 3). Elimina token, id, username,
     * rol y datos de contacto directamente del almacenamiento persistente del dispositivo.
     */
    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_FILE_NAME = "fake_store_secure_session"
        private const val KEY_TOKEN = "key_token"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USERNAME = "key_username"
        private const val KEY_ROLE = "key_role"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_PHONE = "key_phone"
        private const val KEY_ADDRESS = "key_address"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager =
            instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
    }
}