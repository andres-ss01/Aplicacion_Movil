package com.example.fakestoreapp.util

/**
 * Envoltura genérica para representar el estado de una operación asíncrona
 * (carga / éxito / error) y así pilotar los indicadores visuales exigidos
 * en US03, US04 y US05 (spinners, mensajes de error, reintentos).
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
