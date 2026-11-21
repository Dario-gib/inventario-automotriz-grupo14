package com.example.dsy1105_006d_lc7.data.remote

// Forma del proveedor tal como llega desde la API
data class ProveedorDto(
    val codigo: String,
    val nombre: String,
    val correo: String,
    val telefono: String? = null,
    val categorias: List<String>? = null,
    val estado: String? = null
)