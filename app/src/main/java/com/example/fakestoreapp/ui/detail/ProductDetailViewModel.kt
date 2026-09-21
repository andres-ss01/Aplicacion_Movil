package com.example.fakestoreapp.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fakestoreapp.data.model.Product
import com.example.fakestoreapp.data.model.UserRole
import com.example.fakestoreapp.data.repository.ProductRepository
import com.example.fakestoreapp.data.session.SessionManager
import com.example.fakestoreapp.util.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel del detalle de producto (US05). El rol se lee siempre de forma local
 * desde SessionManager (nunca se delega esa validación a la Fake Store API, tal
 * como exige la regla de negocio de US05).
 */
class ProductDetailViewModel(
    private val productId: Int,
    private val repository: ProductRepository = ProductRepository(),
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _product = MutableLiveData<Resource<Product>>()
    val product: LiveData<Resource<Product>> = _product

    val currentRole: UserRole
        get() = sessionManager.getRole()

    val isAdmin: Boolean
        get() = currentRole == UserRole.ADMINISTRADOR

    fun loadProduct() {
        _product.value = Resource.Loading
        viewModelScope.launch {
            _product.value = repository.getProductById(productId)
        }
    }
}
