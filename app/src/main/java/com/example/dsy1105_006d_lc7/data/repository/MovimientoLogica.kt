package com.example.dsy1105_006d_lc7.data.repository

enum class TipoMovimiento(val etiqueta: String, val descripcion: String) {
    INGRESO("Ingreso", "Compra o recepción"),
    VENTA("Venta", "Venta al cliente"),
    CONSUMO("Consumo", "Uso en servicio mecánico"),
    AJUSTE("Ajuste", "Corrección por conteo: indica la cantidad real"),
    DEVOLUCION("Devolución", "Producto devuelto");

    // Las salidas necesitan stock suficiente
    val esSalida: Boolean get() = this == VENTA || this == CONSUMO
}

sealed class ResultadoMovimiento {
    data class Ok(
        val nuevoStock: Int,
        val variacion: Int,
        val quedoConStockBajo: Boolean
    ) : ResultadoMovimiento()

    data class Error(val mensaje: String) : ResultadoMovimiento()
}

// Regla de negocio: calcula cómo queda el stock o explica por qué no se puede
fun calcularMovimiento(
    tipo: TipoMovimiento,
    stockActual: Int,
    stockMinimo: Int,
    cantidad: Int
): ResultadoMovimiento {
    if (cantidad < 0) {
        return ResultadoMovimiento.Error("La cantidad no puede ser negativa")
    }
    if (tipo != TipoMovimiento.AJUSTE && cantidad == 0) {
        return ResultadoMovimiento.Error("La cantidad debe ser mayor a 0")
    }
    if (tipo.esSalida && cantidad > stockActual) {
        return ResultadoMovimiento.Error(
            "Stock insuficiente: hay $stockActual unidades disponibles"
        )
    }
    val nuevoStock = when (tipo) {
        TipoMovimiento.INGRESO, TipoMovimiento.DEVOLUCION -> stockActual + cantidad
        TipoMovimiento.VENTA, TipoMovimiento.CONSUMO -> stockActual - cantidad
        TipoMovimiento.AJUSTE -> cantidad
    }
    return ResultadoMovimiento.Ok(
        nuevoStock = nuevoStock,
        variacion = nuevoStock - stockActual,
        quedoConStockBajo = nuevoStock <= stockMinimo
    )
}