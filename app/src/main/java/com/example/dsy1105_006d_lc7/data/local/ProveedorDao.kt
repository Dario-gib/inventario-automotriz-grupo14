package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ProveedorDao {

    @Query("SELECT * FROM proveedores ORDER BY nombre ASC")
    abstract fun observarTodos(): Flow<List<ProveedorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarTodos(proveedores: List<ProveedorEntity>)

    @Query("DELETE FROM proveedores")
    abstract suspend fun borrarTodos()

    // Reemplaza la copia local en una sola transacción
    @Transaction
    open suspend fun reemplazarTodos(proveedores: List<ProveedorEntity>) {
        borrarTodos()
        insertarTodos(proveedores)
    }
}