package com.example.dsy1105_006d_lc7.ui.proveedor

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.remote.RetrofitClient
import com.example.dsy1105_006d_lc7.data.repository.ProveedorRepository

class ProveedorViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getDatabase(application)
        val repo = ProveedorRepository(RetrofitClient.proveedorApi, db.proveedorDao())
        return ProveedorViewModel(repo) as T
    }
}