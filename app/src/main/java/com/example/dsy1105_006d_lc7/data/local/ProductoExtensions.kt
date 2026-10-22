package com.example.dsy1105_006d_lc7.data.local

// Un producto tiene stock bajo cuando la cantidad es menor o igual al mínimo
val ProductoEntity.stockBajo: Boolean
    get() = cantidad <= stockMinimo