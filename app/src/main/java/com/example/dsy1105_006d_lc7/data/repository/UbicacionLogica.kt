package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.UbicacionEntity

// Un producto junto a su ubicación en bodega (si ya tiene una)
data class ProductoConUbicacion(
    val producto: ProductoEntity,
    val ubicacion: UbicacionEntity?
)

val UbicacionEntity.texto: String
    get() = "Zona $zona · Estante $estante · Repisa $repisa"

fun ProductoConUbicacion.textoUbicacion(): String =
    ubicacion?.texto ?: "Sin ubicación asignada"

fun combinarProductosConUbicacion(
    productos: List<ProductoEntity>,
    ubicaciones: List<UbicacionEntity>
): List<ProductoConUbicacion> {
    val porProducto = ubicaciones.associateBy { it.productoId }
    return productos.map { ProductoConUbicacion(it, porProducto[it.id]) }
}

// Busca por nombre, código o ubicación, sin importar tildes ni mayúsculas
fun filtrarPorTexto(lista: List<ProductoConUbicacion>, texto: String): List<ProductoConUbicacion> {
    val q = normalizar(texto)
    if (q.isBlank()) return lista
    return lista.filter { item ->
        normalizar(item.producto.nombre).contains(q) ||
                normalizar(item.producto.codigo).contains(q) ||
                normalizar(item.textoUbicacion()).contains(q)
    }
}