package com.example.dsy1105_006d_lc7.ui.movimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.MovimientoEntity
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.repository.MovimientoRepository
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import com.example.dsy1105_006d_lc7.data.repository.ResultadoMovimiento
import com.example.dsy1105_006d_lc7.data.repository.TipoMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovimientoFormState(
    val productoId: Int? = null,
    val tipo: TipoMovimiento = TipoMovimiento.INGRESO,
    val cantidad: String = "",
    val nota: String = "",
    val productoError: String? = null,
    val cantidadError: String? = null
)

data class MovimientoDetalle(
    val movimiento: MovimientoEntity,
    val producto: String
)

data class MovimientosUiState(
    val productos: List<ProductoEntity> = emptyList(),
    val historial: List<MovimientoDetalle> = emptyList(),
    val form: MovimientoFormState = MovimientoFormState(),
    val mensaje: String? = null
)

class MovimientoViewModel(
    productoRepository: ProductoRepository,
    private val movimientoRepository: MovimientoRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(MovimientosUiState())
    val estado: StateFlow<MovimientosUiState> = _estado.asStateFlow()

    init {
        // Productos e historial siempre actualizados desde Room
        viewModelScope.launch {
            combine(
                productoRepository.observarTodos(),
                movimientoRepository.historial
            ) { productos, movimientos ->
                val detalle = movimientos.map { m ->
                    val p = productos.firstOrNull { it.id == m.productoId }
                    MovimientoDetalle(
                        movimiento = m,
                        producto = p?.let { "${it.nombre} (${it.codigo})" } ?: "Producto eliminado"
                    )
                }
                productos to detalle
            }.collect { (productos, detalle) ->
                _estado.update { it.copy(productos = productos, historial = detalle) }
            }
        }
    }

    private fun cambiarForm(cambio: (MovimientoFormState) -> MovimientoFormState) {
        _estado.update { it.copy(form = cambio(it.form)) }
    }

    fun onProductoSeleccionado(id: Int) =
        cambiarForm { it.copy(productoId = id, productoError = null, cantidadError = null) }

    fun onTipoChange(tipo: TipoMovimiento) =
        cambiarForm { it.copy(tipo = tipo, cantidadError = null) }

    fun onCantidadChange(valor: String) =
        cambiarForm { it.copy(cantidad = valor, cantidadError = null) }

    fun onNotaChange(valor: String) =
        cambiarForm { it.copy(nota = valor) }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }

    fun registrar() {
        val f = _estado.value.form
        val productoId = f.productoId
        val cantidad = f.cantidad.trim().toIntOrNull()

        val productoError = if (productoId == null) "Selecciona un producto" else null
        val cantidadError = when {
            f.cantidad.isBlank() -> "Campo obligatorio"
            cantidad == null -> "Debe ser un número entero"
            cantidad < 0 -> "Debe ser mayor o igual a 0"
            else -> null
        }
        if (productoId == null || cantidad == null || cantidadError != null) {
            cambiarForm { it.copy(productoError = productoError, cantidadError = cantidadError) }
            return
        }

        viewModelScope.launch {
            when (val r = movimientoRepository.registrar(productoId, f.tipo, cantidad, f.nota.trim())) {
                is ResultadoMovimiento.Ok -> {
                    val aviso = if (r.quedoConStockBajo)
                        " Atención: el producto quedó con stock bajo; revisa Reposición."
                    else ""
                    _estado.update {
                        it.copy(
                            form = it.form.copy(cantidad = "", nota = "", cantidadError = null),
                            mensaje = "Movimiento registrado (${f.tipo.etiqueta}). " +
                                    "Stock actual: ${r.nuevoStock}.$aviso"
                        )
                    }
                }
                is ResultadoMovimiento.Error ->
                    cambiarForm { it.copy(cantidadError = r.mensaje) }
            }
        }
    }
}