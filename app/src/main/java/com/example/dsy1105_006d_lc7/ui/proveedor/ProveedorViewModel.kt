package com.example.dsy1105_006d_lc7.ui.proveedor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import com.example.dsy1105_006d_lc7.data.repository.ProveedorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProveedoresUiState(
    val proveedores: List<ProveedorEntity> = emptyList(),
    val cargando: Boolean = false,
    val mensaje: String? = null
)

class ProveedorViewModel(
    private val repository: ProveedorRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(ProveedoresUiState())
    val estado: StateFlow<ProveedoresUiState> = _estado.asStateFlow()

    init {
        // Siempre se muestra lo guardado en Room
        viewModelScope.launch {
            repository.proveedores.collect { lista ->
                _estado.update { it.copy(proveedores = lista) }
            }
        }
        sincronizar()
    }

    // Consulta la API REST y actualiza la copia local
    fun sincronizar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensaje = null) }
            val ok = repository.sincronizar()
            _estado.update {
                it.copy(
                    cargando = false,
                    mensaje = if (ok) "Proveedores actualizados desde la API"
                    else "Sin conexión: se muestran los datos guardados"
                )
            }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }
}