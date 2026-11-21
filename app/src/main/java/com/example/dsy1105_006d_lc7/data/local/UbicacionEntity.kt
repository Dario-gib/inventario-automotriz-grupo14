package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ubicaciones")
data class UbicacionEntity(
    @PrimaryKey val productoId: Int,
    val zona: String,
    val estante: String,
    val repisa: String
)