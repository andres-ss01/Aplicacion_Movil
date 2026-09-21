package com.example.fakestoreapp.data.session

import com.example.fakestoreapp.data.model.Product

/**
 * Gestor de estado global mínimo para el carrito local. Aunque el carrito como
 * funcionalidad completa no forma parte de las 5 historias de usuario, US02 exige
 * explícitamente que cualquier gestor de estado global se reinicie a cero durante
 * el cierre de sesión, para que el siguiente usuario no herede datos del anterior.
 */
object CartManager {

    private val items = mutableListOf<Product>()

    fun addItem(product: Product) {
        items.add(product)
    }

    fun getItems(): List<Product> = items.toList()

    /** Reinicia el carrito a cero (US02 - Escenario 3 / "Reinicio de estados"). */
    fun clear() {
        items.clear()
    }
}
