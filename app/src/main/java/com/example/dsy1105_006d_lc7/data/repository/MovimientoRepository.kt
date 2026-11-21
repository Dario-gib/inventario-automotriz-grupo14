package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.MovimientoDao
import com.example.dsy1105_006d_lc7.data.local.MovimientoEntity
import com.example.dsy1105_006d_lc7.data.local.ProductoDao
import kotlinx.coroutines.flow.Flow

class MovimientoRepository(
    private val productoDao: ProductoDao,
    private val movimientoDao: MovimientoDao
) {
    val historial: Flow<List<MovimientoEntity>> = movimientoDao.observarTodos()

    // Lee el stock real desde la base de datos, aplica la regla y, si corresponde, guarda
    suspend fun registrar(
        productoId: Int,
        tipo: TipoMovimiento,
        cantidad: Int,
        nota: String
    ): ResultadoMovimiento {
        val producto = productoDao.obtenerPorId(productoId)
            ?: return ResultadoMovimiento.Error("El producto ya no existe")

        val resultado = calcularMovimiento(tipo, producto.cantidad, producto.stockMinimo, cantidad)

        if (resultado is ResultadoMovimiento.Ok) {
            movimientoDao.registrarMovimiento(
                MovimientoEntity(
                    productoId = productoId,
                    tipo = tipo.name,
                    cantidad = resultado.variacion,
                    nota = nota
                ),
                resultado.nuevoStock
            )
        }
        return resultado
    }
}