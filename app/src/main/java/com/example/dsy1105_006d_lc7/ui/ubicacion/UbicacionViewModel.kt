package com.example.dsy1105_006d_lc7.ui.ubicacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.UbicacionEntity
import com.example.dsy1105_006d_lc7.data.repository.ProductoConUbicacion
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import com.example.dsy1105_006d_lc7.data.repository.UbicacionRepository
import com.example.dsy1105_006d_lc7.data.repository.combinarProductosConUbicacion
import com.example.dsy1105_006d_lc7.data.repository.filtrarPorTexto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Formulario para cambiar la ubicación de un producto
data class UbicacionFormState(
    val producto: ProductoEntity,
    val zona: String = "",
    val estante: String = "",
    val repisa: String = "",
    val zonaError: String? = null,
    val estanteError: String? = null,
    val repisaError: String? = null
)

data class UbicacionesUiState(
    val todos: List<ProductoConUbicacion> = emptyList(),
    val busqueda: String = "",
    val edicion: UbicacionFormState? = null,
    val mensaje: String? = null
) {
    // Lo que se ve en pantalla, según la búsqueda
    val items: List<ProductoConUbicacion>
        get() = filtrarPorTexto(todos, busqueda)
}

class UbicacionViewModel(
    productoRepository: ProductoRepository,
    private val ubicacionRepository: UbicacionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(UbicacionesUiState())
    val estado: StateFlow<UbicacionesUiState> = _estado.asStateFlow()

    init {
        // Productos y ubicaciones siempre actualizados desde Room
        viewModelScope.launch {
            combine(
                productoRepository.observarTodos(),
                ubicacionRepository.ubicaciones
            ) { productos, ubicaciones ->
                combinarProductosConUbicacion(productos, ubicaciones)
            }.collect { lista ->
                _estado.update { it.copy(todos = lista) }
            }
        }
    }

    fun onBusquedaChange(texto: String) {
        _estado.update { it.copy(busqueda = texto) }
    }

    // Abre el formulario con la ubicación actual (si tiene)
    fun editar(item: ProductoConUbicacion) {
        _estado.update {
            it.copy(
                edicion = UbicacionFormState(
                    producto = item.producto,
                    zona = item.ubicacion?.zona.orEmpty(),
                    estante = item.ubicacion?.estante.orEmpty(),
                    repisa = item.ubicacion?.repisa.orEmpty()
                )
            )
        }
    }

    fun cancelarEdicion() {
        _estado.update { it.copy(edicion = null) }
    }

    private fun cambiarEdicion(cambio: (UbicacionFormState) -> UbicacionFormState) {
        _estado.update { s -> s.edicion?.let { s.copy(edicion = cambio(it)) } ?: s }
    }

    fun onZonaChange(valor: String) = cambiarEdicion { it.copy(zona = valor, zonaError = null) }
    fun onEstanteChange(valor: String) = cambiarEdicion { it.copy(estante = valor, estanteError = null) }
    fun onRepisaChange(valor: String) = cambiarEdicion { it.copy(repisa = valor, repisaError = null) }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }

    fun guardar() {
        val f = _estado.value.edicion ?: return

        val zonaError = if (f.zona.isBlank()) "La zona es obligatoria" else null
        val estanteError = if (f.estante.isBlank()) "El estante es obligatorio" else null
        val repisaError = if (f.repisa.isBlank()) "La repisa es obligatoria" else null

        if (zonaError != null || estanteError != null || repisaError != null) {
            cambiarEdicion {
                it.copy(zonaError = zonaError, estanteError = estanteError, repisaError = repisaError)
            }
            return
        }

        viewModelScope.launch {
            ubicacionRepository.guardar(
                UbicacionEntity(
                    productoId = f.producto.id,
                    zona = f.zona.trim(),
                    estante = f.estante.trim(),
                    repisa = f.repisa.trim()
                )
            )
            _estado.update {
                it.copy(edicion = null, mensaje = "Ubicación de ${f.producto.nombre} actualizada")
            }
        }
    }
}