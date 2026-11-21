package com.example.dsy1105_006d_lc7.data.remote

data class ProveedorDto(
    val codigo: String,
    val nombre: String,
    val correo: String,
    val telefono: String? = null,
    val categorias: List<String> = emptyList(),
    val estado: String = "Activo"
)