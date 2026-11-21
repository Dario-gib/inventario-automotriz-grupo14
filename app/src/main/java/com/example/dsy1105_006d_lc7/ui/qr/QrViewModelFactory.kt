package com.example.dsy1105_006d_lc7.ui.qr

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository

class QrViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = ProductoRepository(AppDatabase.getDatabase(application).productoDao())
        return QrViewModel(repo) as T
    }
}