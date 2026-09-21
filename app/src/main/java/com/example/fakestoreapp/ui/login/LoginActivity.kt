package com.example.fakestoreapp.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.fakestoreapp.data.session.SessionManager
import com.example.fakestoreapp.databinding.ActivityLoginBinding
import com.example.fakestoreapp.ui.main.MainActivity
import com.example.fakestoreapp.util.Resource
import com.example.fakestoreapp.util.ViewModelFactory
import com.google.android.material.snackbar.Snackbar

/**
 * Punto de entrada de la app (US01). Si ya existe una sesión válida guardada de forma
 * local, salta directo al catálogo. En caso contrario, valida conectividad antes de
 * llamar a /auth/login (US01 - Escenario 3) y, al autenticar, mapea el rol localmente.
 *
 * También cumple el requisito de US02 (Escenario 2): al llegar aquí después de un
 * logout no se puede regresar al catálogo con el botón "Atrás"; esta pantalla es el
 * fondo de la pila y el botón "Atrás" simplemente cierra la app.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager.getInstance(applicationContext)

        // Si ya hay sesión activa, no mostramos el login de nuevo.
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory { LoginViewModel(sessionManager = sessionManager) }
        )[LoginViewModel::class.java]

        setupBackPressedHandler()
        setupListeners()
        observeViewModel()
    }

    private fun setupBackPressedHandler() {
        // LoginActivity es la raíz de la pila: "Atrás" cierra la app en vez de
        // navegar a cualquier pantalla protegida.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            hideError()
            val username = binding.etUsername.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()

            if (!com.example.fakestoreapp.util.NetworkUtils.isConnected(this)) {
                // US01 - Escenario 3: detener la ejecución ANTES de llamar a la API.
                showError(getString(com.example.fakestoreapp.R.string.login_error_no_connection))
                return@setOnClickListener
            }

            viewModel.login(username, password)
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success -> {
                    setLoading(false)
                    navigateToMain()
                }
                is Resource.Error -> {
                    setLoading(false)
                    val message = when (state.message) {
                        com.example.fakestoreapp.data.repository.AuthRepository.ERROR_INVALID_CREDENTIALS ->
                            getString(com.example.fakestoreapp.R.string.login_error_invalid)
                        com.example.fakestoreapp.data.repository.AuthRepository.ERROR_NETWORK ->
                            getString(com.example.fakestoreapp.R.string.login_error_no_connection)
                        else -> getString(com.example.fakestoreapp.R.string.login_error_generic)
                    }
                    showError(message)
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = android.view.View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = android.view.View.GONE
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            // Limpia cualquier posible historial previo, de forma que el catálogo
            // sea la nueva raíz mientras haya sesión activa.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
