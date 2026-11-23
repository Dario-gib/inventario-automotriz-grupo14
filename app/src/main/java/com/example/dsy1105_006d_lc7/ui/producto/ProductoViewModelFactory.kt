package com.example.dsy1105_006d_lc7.ui.producto

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import com.example.dsy1105_006d_lc7.data.repository.UbicacionRepository

// Crea el ProductoViewModel con el repositorio real (en las pruebas se usa uno simulado)
class ProductoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getDatabase(application)
        val repo = ProductoRepository(db.productoDao())
        val ubicaciones = UbicacionRepository(db.ubicacionDao()).ubicaciones
        return ProductoViewModel(repo, ubicaciones) as T
    }
}