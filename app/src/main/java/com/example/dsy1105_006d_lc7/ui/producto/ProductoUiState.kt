package com.example.dsy1105_006d_lc7.ui.producto

import com.example.dsy1105_006d_lc7.data.local.ProductoEntity

// Estado del formulario (lo que se escribe + errores), No es la tabla de Room
data class ProductoUiState(
    val id: Int = 0,
    val codigo: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val cantidad: String = "",
    val stockMinimo: String = "",

    val codigoError: String? = null,
    val nombreError: String? = null,
    val categoriaError: String? = null,
    val cantidadError: String? = null,
    val stockMinimoError: String? = null,

    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
) {
    val esEdicion: Boolean get() = id != 0
}

// Estado de la pantalla de lista
data class ListaProductosUiState(
    val productos: List<ProductoEntity> = emptyList(),
    val busqueda: String = ""
)