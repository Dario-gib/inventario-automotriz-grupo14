package com.example.dsy1105_006d_lc7.ui.reposicion

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.remote.RetrofitClient
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import com.example.dsy1105_006d_lc7.data.repository.ProveedorRepository

class ReposicionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getDatabase(application)
        val productos = ProductoRepository(db.productoDao())
        val proveedores = ProveedorRepository(RetrofitClient.proveedorApi, db.proveedorDao())
        return ReposicionViewModel(productos, proveedores) as T
    }
}