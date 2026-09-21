package com.example.fakestoreapp.data.repository

import com.example.fakestoreapp.data.api.ApiService
import com.example.fakestoreapp.data.api.RetrofitClient
import com.example.fakestoreapp.data.model.Product
import com.example.fakestoreapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repositorio de productos. Cubre US03 (catálogo general), US04 (categorías y filtro)
 * y US05 (detalle de un producto).
 */
class ProductRepository(private val api: ApiService = RetrofitClient.apiService) {

    suspend fun getAllProducts(): Resource<List<Product>> = withContext(Dispatchers.IO) {
        safeCall { api.getAllProducts() }
    }

    suspend fun getCategories(): Resource<List<String>> = withContext(Dispatchers.IO) {
        safeCall { api.getCategories() }
    }

    suspend fun getProductsByCategory(category: String): Resource<List<Product>> =
        withContext(Dispatchers.IO) {
            safeCall { api.getProductsByCategory(category) }
        }

    suspend fun getProductById(id: Int): Resource<Product> = withContext(Dispatchers.IO) {
        safeCall { api.getProductById(id) }
    }

    private suspend inline fun <T> safeCall(call: () -> retrofit2.Response<T>): Resource<T> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Resource.Success(body)
            } else {
                Resource.Error("NOT_FOUND")
            }
        } catch (io: IOException) {
            Resource.Error("NETWORK_ERROR")
        } catch (e: Exception) {
            Resource.Error("GENERIC_ERROR")
        }
    }
}
