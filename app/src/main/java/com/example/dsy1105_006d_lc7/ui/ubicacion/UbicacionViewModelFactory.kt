package com.example.dsy1105_006d_lc7.ui.ubicacion

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import com.example.dsy1105_006d_lc7.data.repository.UbicacionRepository

class UbicacionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getDatabase(application)
        val productos = ProductoRepository(db.productoDao())
        val ubicaciones = UbicacionRepository(db.ubicacionDao())
        return UbicacionViewModel(productos, ubicaciones) as T
    }
}