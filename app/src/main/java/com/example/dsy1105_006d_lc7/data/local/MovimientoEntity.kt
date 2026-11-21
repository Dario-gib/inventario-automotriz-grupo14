package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// tipo: INGRESO, VENTA, CONSUMO, AJUSTE o DEVOLUCION
@Entity(tableName = "movimientos", indices = [Index("productoId")])
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productoId: Int,
    val tipo: String,
    val cantidad: Int,
    val nota: String = "",
    val fecha: Long = System.currentTimeMillis()
)