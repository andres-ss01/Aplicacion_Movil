package com.example.fakestoreapp.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.fakestoreapp.R
import com.example.fakestoreapp.data.model.Product
import com.example.fakestoreapp.data.session.SessionManager
import com.example.fakestoreapp.databinding.FragmentProductDetailBinding
import com.example.fakestoreapp.databinding.LayoutAdminControlsBinding
import com.example.fakestoreapp.util.Resource
import com.example.fakestoreapp.util.ViewModelFactory
import java.util.Locale

/**
 * Vista de detalle con interfaz dinámica según el rol de sesión (US05):
 *  - Cliente / Auditor: solo información de consulta (Escenario 1).
 *  - Administrador: además ve los botones "Editar" y "Eliminar", inflados
 *    dinámicamente vía ViewStub (Escenario 2), nunca instanciados ocultos.
 *  - Cualquier error de la API (o producto inexistente) muestra una alerta y
 *    regresa automáticamente al catálogo (Escenario 3).
 */
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val args: ProductDetailFragmentArgs by navArgs()

    private val viewModel: ProductDetailViewModel by viewModels {
        ViewModelFactory {
            ProductDetailViewModel(
                productId = args.productId,
                sessionManager = SessionManager.getInstance(requireContext().applicationContext)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Habilitar flecha de regreso en la ActionBar de la Activity superior
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        observeViewModel()
        if (savedInstanceState == null) {
            viewModel.loadProduct()
        }
    }

    private fun observeViewModel() {
        viewModel.product.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentContainer.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    bindProduct(state.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    handleError()
                }
            }
        }
    }

    private fun bindProduct(product: Product) {
        binding.contentContainer.visibility = View.VISIBLE
        (activity as? AppCompatActivity)?.supportActionBar?.title = product.title

        Glide.with(binding.ivDetailImage.context)
            .load(product.image)
            .centerInside()
            .into(binding.ivDetailImage)

        binding.tvDetailCategory.text = product.category.replaceFirstChar { it.uppercase() }
        binding.tvDetailTitle.text = product.title
        binding.tvDetailPrice.text = String.format(Locale.US, getString(R.string.detail_price_format), product.price)
        binding.tvDetailDescription.text = product.description

        // Escenario 2 (US05): el ViewStub SOLO se infla si el rol es Administrador.
        // Para Cliente/Auditor, btnEdit y btnDelete nunca llegan a existir en el árbol
        // de vistas (no se ocultan con visibility=GONE, se excluyen por completo).
        if (viewModel.isAdmin && binding.stubAdminControls.parent != null) {
            val inflatedView = binding.stubAdminControls.inflate()
            val adminBinding = LayoutAdminControlsBinding.bind(inflatedView)
            setupAdminControls(adminBinding, product)
        }
    }

    private fun setupAdminControls(adminBinding: LayoutAdminControlsBinding, product: Product) {
        adminBinding.btnEdit.setOnClickListener {
            Toast.makeText(requireContext(), R.string.detail_edit_toast, Toast.LENGTH_SHORT).show()
        }
        adminBinding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.detail_delete_confirm)
                .setPositiveButton(R.string.dialog_yes) { _, _ ->
                    Toast.makeText(requireContext(), R.string.detail_delete_toast, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .setNegativeButton(R.string.dialog_no, null)
                .show()
        }
    }

    /**
     * Manejar el toque en la flecha de la ActionBar para volver al catálogo
     */
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * US05 - Escenario 3: la solicitud a la API falla o el producto no existe ->
     * alerta "Producto no disponible" + regreso automático al catálogo.
     */
    private fun handleError() {
        Toast.makeText(requireContext(), R.string.detail_error_not_available, Toast.LENGTH_LONG).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Ocultar la flecha de navegación al salir del detalle para no dejarla activa en el catálogo
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(false)
        }
        _binding = null
    }
}