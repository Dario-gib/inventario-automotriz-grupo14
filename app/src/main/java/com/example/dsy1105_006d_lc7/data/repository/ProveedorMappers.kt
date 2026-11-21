package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import com.example.dsy1105_006d_lc7.data.remote.ProveedorDto

// De lo que llega por la API a lo que se guarda en Room
fun ProveedorDto.toEntity() = ProveedorEntity(
    codigo = codigo,
    nombre = nombre,
    correo = correo,
    telefono = telefono.orEmpty(),
    categorias = categorias.orEmpty().joinToString(","),
    estado = estado ?: "Activo"
)

val ProveedorEntity.listaCategorias: List<String>
    get() = categorias.split(",").map { it.trim() }.filter { it.isNotBlank() }

val ProveedorEntity.activo: Boolean
    get() = estado.equals("Activo", ignoreCase = true)