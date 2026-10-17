package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Insert
    suspend fun insertar(producto: ProductoEntity)

    @Update
    suspend fun actualizar(producto: ProductoEntity)

    @Delete
    suspend fun eliminar(producto: ProductoEntity)

    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun observarTodos(): Flow<List<ProductoEntity>>

    @Query(
        "SELECT * FROM productos " +
                "WHERE nombre LIKE '%' || :texto || '%' OR codigo LIKE '%' || :texto || '%' " +
                "ORDER BY nombre ASC"
    )
    fun buscar(texto: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ProductoEntity?

    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    suspend fun obtenerPorCodigo(codigo: String): ProductoEntity?
}