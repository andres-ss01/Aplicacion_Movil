package com.example.fakestoreapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fakestoreapp.data.repository.AuthRepository

import com.example.fakestoreapp.data.session.SessionManager
import com.example.fakestoreapp.util.Resource
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<Unit>>()
    val loginState: LiveData<Resource<Unit>> = _loginState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = Resource.Error(AuthRepository.ERROR_INVALID_CREDENTIALS)
            return
        }

        _loginState.value = Resource.Loading

        viewModelScope.launch {
            when (val result = authRepository.login(username.trim(), password)) {
                is Resource.Success -> {
                    val auth = result.data

                    // Guardamos la sesión completa agregando email, phone y address
                    sessionManager.saveSession(
                        token = auth.token,
                        userId = auth.userId,
                        username = auth.username,
                        role = auth.role,
                        email = auth.email,
                        phone = auth.phone,
                        address = auth.address
                    )

                    _loginState.value = Resource.Success(Unit)
                }
                is Resource.Error -> {
                    _loginState.value = Resource.Error(result.message)
                }
                is Resource.Loading -> {
                    _loginState.value = Resource.Loading
                }
            }
        }
    }
}