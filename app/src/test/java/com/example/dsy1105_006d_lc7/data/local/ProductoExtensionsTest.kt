package com.example.dsy1105_006d_lc7.data.local

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ProductoExtensionsTest : StringSpec({

    fun producto(cantidad: Int, minimo: Int) = ProductoEntity(
        codigo = "TST-001",
        nombre = "Producto de prueba",
        categoria = "Motor",
        cantidad = cantidad,
        stockMinimo = minimo
    )

    "hay stock bajo cuando la cantidad es menor al mínimo" {
        producto(2, 5).stockBajo shouldBe true
    }

    "hay stock bajo cuando la cantidad es igual al mínimo" {
        producto(5, 5).stockBajo shouldBe true
    }

    "no hay stock bajo cuando la cantidad supera el mínimo" {
        producto(6, 5).stockBajo shouldBe false
    }

    "la cantidad sugerida es el doble del mínimo menos lo que hay" {
        producto(2, 5).cantidadSugerida shouldBe 8
    }

    "la cantidad sugerida nunca es menor a 1" {
        producto(20, 5).cantidadSugerida shouldBe 1
    }
})