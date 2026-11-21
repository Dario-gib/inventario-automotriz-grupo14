package com.example.dsy1105_006d_lc7.ui.reposicion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.cantidadSugerida
import com.example.dsy1105_006d_lc7.data.repository.GrupoReposicion
import com.example.dsy1105_006d_lc7.data.repository.PlanReposicion
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import com.example.dsy1105_006d_lc7.data.repository.ProveedorRepository
import com.example.dsy1105_006d_lc7.data.repository.agruparPorProveedor
import com.example.dsy1105_006d_lc7.ui.producto.BorradorCorreo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReposicionUiState(
    val plan: PlanReposicion = PlanReposicion(),
    val cargando: Boolean = false,
    val mensaje: String? = null
)

class ReposicionViewModel(
    private val productoRepository: ProductoRepository,
    private val proveedorRepository: ProveedorRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(ReposicionUiState())
    val estado: StateFlow<ReposicionUiState> = _estado.asStateFlow()

    init {
        // El plan se recalcula solo cuando cambian los productos o los proveedores
        viewModelScope.launch {
            combine(
                productoRepository.observarTodos(),
                proveedorRepository.proveedores
            ) { productos, proveedores ->
                agruparPorProveedor(productos, proveedores)
            }.collect { plan ->
                _estado.update { it.copy(plan = plan) }
            }
        }
        sincronizar()
    }

    // Consulta la API REST de proveedores y actualiza la copia local
    fun sincronizar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensaje = null) }
            val ok = proveedorRepository.sincronizar()
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

    // Borrador de correo para un proveedor, con sus productos y cantidades sugeridas
    fun generarBorrador(grupo: GrupoReposicion): BorradorCorreo {
        val detalle = grupo.productos.joinToString("\n") { p ->
            "- ${p.nombre} (código ${p.codigo}): disponible ${p.cantidad}, " +
                    "solicitar ${p.cantidadSugerida} unidades"
        }
        val cuerpo = "Estimado equipo de ${grupo.proveedor.nombre}:\n\n" +
                "Junto con saludar, solicitamos disponibilidad y cotización " +
                "de los siguientes productos:\n\n" +
                detalle +
                "\n\nQuedamos atentos a su respuesta.\n\n" +
                "Saludos cordiales,\nTaller PuenteAuto (datos ficticios)"
        return BorradorCorreo(
            destinatario = grupo.proveedor.correo,
            asunto = "Solicitud de cotización de reposición",
            cuerpo = cuerpo
        )
    }
}