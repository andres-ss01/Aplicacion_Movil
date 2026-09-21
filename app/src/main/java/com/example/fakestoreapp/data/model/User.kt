package com.example.fakestoreapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo que empata con la estructura devuelta por GET /users de la Fake Store API.
 * Se usa únicamente para localizar el id del usuario autenticado a partir de su
 * "username" y así poder mapear su rol de forma local (US01).
 */
data class User(
    val id: Int,
    val email: String? = null,
    val username: String,
    val password: String? = null,
    val name: Name? = null,
    val phone: String? = null,
    val address: Address? = null
)

data class Name(
    val firstname: String? = null,
    val lastname: String? = null
)

data class Address(
    val city: String? = null,
    val street: String? = null,
    val number: Int? = null,
    val zipcode: String? = null
) {
    // Propiedad calculada para obtener la dirección formateada en la UI
    val fullAddress: String
        get() {
            val parts = listOfNotNull(
                street?.takeIf { it.isNotBlank() },
                number?.let { "#$it" },
                city?.takeIf { it.isNotBlank() }
            )
            return if (parts.isNotEmpty()) parts.joinToString(", ") else "No disponible"
        }
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

/**
 * Roles soportados por la aplicación. El mapeo se realiza estrictamente en el
 * código base, según la regla de negocio de US01:
 *  - IDs 1 y 2  -> ADMIN
 *  - ID 3       -> AUDITOR
 *  - Resto      -> CLIENTE
 */
enum class UserRole {
    ADMINISTRADOR,
    AUDITOR,
    CLIENTE;

    companion object {
        fun fromUserId(userId: Int): UserRole = when (userId) {
            1, 2 -> ADMINISTRADOR
            3 -> AUDITOR
            else -> CLIENTE
        }
    }
}