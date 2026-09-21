package com.example.fakestoreapp.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Fábrica genérica y ligera para inyectar dependencias (SessionManager, repositorios)
 * en los ViewModels sin necesidad de añadir Hilt/Dagger a un proyecto de este tamaño.
 */
class ViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <U : ViewModel> create(modelClass: Class<U>): U = creator() as U
}
