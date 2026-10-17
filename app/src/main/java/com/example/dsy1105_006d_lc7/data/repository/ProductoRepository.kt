package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.ProductoDao
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import kotlinx.coroutines.flow.Flow

class ProductoRepository(private val dao: ProductoDao) {

    fun observarTodos(): Flow<List<ProductoEntity>> = dao.observarTodos()

    fun buscar(texto: String): Flow<List<ProductoEntity>> = dao.buscar(texto)

    suspend fun obtenerPorId(id: Int): ProductoEntity? = dao.obtenerPorId(id)

    suspend fun obtenerPorCodigo(codigo: String): ProductoEntity? = dao.obtenerPorCodigo(codigo)

    suspend fun insertar(producto: ProductoEntity) = dao.insertar(producto)

    suspend fun actualizar(producto: ProductoEntity) = dao.actualizar(producto)

    suspend fun eliminar(producto: ProductoEntity) = dao.eliminar(producto)
}