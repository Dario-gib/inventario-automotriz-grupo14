package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import com.example.dsy1105_006d_lc7.data.local.stockBajo
import java.text.Normalizer

// Un proveedor con los productos que se le pedirían
data class GrupoReposicion(
    val proveedor: ProveedorEntity,
    val productos: List<ProductoEntity>
)

data class PlanReposicion(
    val grupos: List<GrupoReposicion> = emptyList(),
    val sinProveedor: List<ProductoEntity> = emptyList()
)

// Quita tildes y mayúsculas: "Baterías" y "baterias" se consideran iguales
internal fun normalizar(texto: String): String =
    Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()

private fun proveedorPara(producto: ProductoEntity, activos: List<ProveedorEntity>): ProveedorEntity? =
    activos.firstOrNull { prov ->
        prov.listaCategorias.any { normalizar(it) == normalizar(producto.categoria) }
    }

// Agrupa por proveedor los productos con stock bajo.
// Solo se consideran proveedores activos y se asigna según la categoría.
fun agruparPorProveedor(
    productos: List<ProductoEntity>,
    proveedores: List<ProveedorEntity>
): PlanReposicion {
    val activos = proveedores.filter { it.activo }
    val conStockBajo = productos.filter { it.stockBajo }

    val grupos = conStockBajo
        .mapNotNull { p -> proveedorPara(p, activos)?.let { prov -> prov to p } }
        .groupBy({ it.first }, { it.second })
        .map { (prov, lista) -> GrupoReposicion(prov, lista) }
        .sortedBy { it.proveedor.nombre }

    val sinProveedor = conStockBajo.filter { proveedorPara(it, activos) == null }

    return PlanReposicion(grupos, sinProveedor)
}