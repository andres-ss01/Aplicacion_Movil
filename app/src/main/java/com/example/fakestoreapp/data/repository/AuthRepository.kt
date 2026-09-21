package com.example.fakestoreapp.data.repository

import com.example.fakestoreapp.data.api.ApiService
import com.example.fakestoreapp.data.api.RetrofitClient
import com.example.fakestoreapp.data.model.LoginRequest
import com.example.fakestoreapp.data.model.UserRole
import com.example.fakestoreapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Resultado de un login exitoso: token + datos necesarios para la sesión local
 * y el perfil de usuario.
 */
data class AuthResult(
    val token: String,
    val userId: Int,
    val username: String,
    val role: UserRole,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null
)

/**
 * Repositorio de autenticación (US01). La Fake Store API solo devuelve un token en
 * /auth/login, así que para mapear el rol localmente (regla de negocio de US01) y
 * recuperar los datos de perfil (email, teléfono, dirección) se consulta /users
 * y se localiza el registro cuyo "username" coincide con el ingresado.
 */
class AuthRepository(private val api: ApiService = RetrofitClient.apiService) {

    suspend fun login(username: String, password: String): Resource<AuthResult> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Petición de autenticación
                val loginResponse = api.login(LoginRequest(username, password))

                if (!loginResponse.isSuccessful || loginResponse.body() == null) {
                    return@withContext Resource.Error(ERROR_INVALID_CREDENTIALS)
                }

                val token = loginResponse.body()!!.token

                // 2. Consulta a /users para obtener la información completa del perfil
                val usersResponse = api.getUsers()
                val matchedUser = usersResponse.body()?.firstOrNull {
                    it.username.equals(username, ignoreCase = true)
                }

                // Determinar el ID y mapear el rol según las reglas de la aplicación
                val userId = matchedUser?.id ?: -1
                val role = UserRole.fromUserId(userId)

                // Formatear dirección combinando sus partes si el usuario existe
                val formattedAddress = matchedUser?.address?.let { addr ->
                    "${addr.street} #${addr.number}, ${addr.city}"
                }

                // 3. Retornar el objeto AuthResult completo con los campos requeridos
                Resource.Success(
                    AuthResult(
                        token = token,
                        userId = userId,
                        username = username,
                        role = role,
                        email = matchedUser?.email,
                        phone = matchedUser?.phone,
                        address = formattedAddress
                    )
                )
            } catch (io: IOException) {
                Resource.Error(ERROR_NETWORK)
            } catch (e: Exception) {
                Resource.Error(ERROR_GENERIC)
            }
        }

    companion object {
        const val ERROR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
        const val ERROR_NETWORK = "NETWORK_ERROR"
        const val ERROR_GENERIC = "GENERIC_ERROR"
    }
}