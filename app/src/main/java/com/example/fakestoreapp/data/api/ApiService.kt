package com.example.fakestoreapp.data.api

import com.example.fakestoreapp.data.model.LoginRequest
import com.example.fakestoreapp.data.model.LoginResponse
import com.example.fakestoreapp.data.model.Product
import com.example.fakestoreapp.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Contrato de la Fake Store API (https://fakestoreapi.com/).
 * Cubre los endpoints requeridos por US01, US03, US04 y US05.
 */
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<User>

    @GET("products")
    suspend fun getAllProducts(): Response<List<Product>>

    @GET("products/categories")
    suspend fun getCategories(): Response<List<String>>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): Response<List<Product>>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<Product>
}
