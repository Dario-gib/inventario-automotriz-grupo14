package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.ProveedorDao
import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import com.example.dsy1105_006d_lc7.data.remote.ProveedorApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class ProveedorRepository(
    private val api: ProveedorApi,
    private val dao: ProveedorDao
) {
    // Lo que se muestra sale siempre de Room, así funciona sin conexión
    val proveedores: Flow<List<ProveedorEntity>> = dao.observarTodos()

    // Consulta la API REST y guarda una copia local. true si funcionó
    suspend fun sincronizar(): Boolean {
        return try {
            val remotos = api.obtenerProveedores()
            dao.reemplazarTodos(remotos.map { it.toEntity() })
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }
}