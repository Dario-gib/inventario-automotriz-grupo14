package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.UbicacionDao
import com.example.dsy1105_006d_lc7.data.local.UbicacionEntity
import kotlinx.coroutines.flow.Flow

class UbicacionRepository(private val dao: UbicacionDao) {

    val ubicaciones: Flow<List<UbicacionEntity>> = dao.observarTodas()

    suspend fun guardar(ubicacion: UbicacionEntity) = dao.guardar(ubicacion)

    suspend fun obtener(productoId: Int): UbicacionEntity? = dao.obtener(productoId)
}