package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UbicacionDao {

    @Query("SELECT * FROM ubicaciones")
    fun observarTodas(): Flow<List<UbicacionEntity>>

    // Si el producto ya tenía ubicación, la reemplaza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(ubicacion: UbicacionEntity)

    @Query("SELECT * FROM ubicaciones WHERE productoId = :productoId")
    suspend fun obtener(productoId: Int): UbicacionEntity?
}