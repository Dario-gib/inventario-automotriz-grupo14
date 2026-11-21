package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MovimientoDao {

    @Insert
    abstract suspend fun insertar(movimiento: MovimientoEntity)

    @Query("UPDATE productos SET cantidad = :cantidad WHERE id = :productoId")
    abstract suspend fun actualizarStock(productoId: Int, cantidad: Int)

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC, id DESC")
    abstract fun observarTodos(): Flow<List<MovimientoEntity>>

    // El movimiento y el nuevo stock se guardan juntos, o no se guarda nada
    @Transaction
    open suspend fun registrarMovimiento(movimiento: MovimientoEntity, nuevoStock: Int) {
        insertar(movimiento)
        actualizarStock(movimiento.productoId, nuevoStock)
    }
}