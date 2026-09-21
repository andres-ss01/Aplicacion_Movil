package com.example.fakestoreapp.data.model

/**
 * Modelo que empata exactamente con la estructura JSON de /products (US03, regla de
 * "Mapeo del JSON").
 */
data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: Rating? = null
)

data class Rating(
    val rate: Double? = null,
    val count: Int? = null
)
