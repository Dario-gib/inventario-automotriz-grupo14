package com.example.dsy1105_006d_lc7.data.repository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MovimientoLogicaTest : StringSpec({

    "un ingreso suma al stock" {
        calcularMovimiento(TipoMovimiento.INGRESO, 10, 5, 5) shouldBe
                ResultadoMovimiento.Ok(nuevoStock = 15, variacion = 5, quedoConStockBajo = false)
    }

    "una devolución suma al stock" {
        calcularMovimiento(TipoMovimiento.DEVOLUCION, 3, 5, 2) shouldBe
                ResultadoMovimiento.Ok(nuevoStock = 5, variacion = 2, quedoConStockBajo = true)
    }

    "una venta resta del stock" {
        calcularMovimiento(TipoMovimiento.VENTA, 10, 5, 4) shouldBe
                ResultadoMovimiento.Ok(nuevoStock = 6, variacion = -4, quedoConStockBajo = false)
    }

    "un consumo resta del stock" {
        calcularMovimiento(TipoMovimiento.CONSUMO, 10, 5, 8) shouldBe
                ResultadoMovimiento.Ok(nuevoStock = 2, variacion = -8, quedoConStockBajo = true)
    }

    "una venta con stock insuficiente da error" {
        calcularMovimiento(TipoMovimiento.VENTA, 3, 5, 10) shouldBe
                ResultadoMovimiento.Error("Stock insuficiente: hay 3 unidades disponibles")
    }

    "un consumo con stock insuficiente da error" {
        calcularMovimiento(TipoMovimiento.CONSUMO, 1, 5, 2) shouldBe
                ResultadoMovimiento.Error("Stock insuficiente: hay 1 unidades disponibles")
    }

    "se puede vender todo el stock disponible" {
        calcularMovimiento(TipoMovimiento.VENTA, 4, 2, 4) shouldBe
                ResultadoMovimiento.Ok(nuevoStock = 0, variacion = -4, quedoConStockBajo = true)
    }

    "una cantidad cero solo se permite en un ajuste" {
        calcularMovimiento(TipoMovimiento.VENTA, 10, 5, 0) shouldBe
                ResultadoMovimiento.Error("La cantidad debe ser mayor a 0")
        calcularMovimiento(TipoMovimiento.AJUSTE, 10, 5, 0) shouldBe
                ResultadoMovimiento.Ok(nuevoStock = 0, variacion = -10, quedoConStockBajo = true)
    }

    "un ajuste fija la cantidad real y calcula la diferencia" {
        calcularMovimiento(TipoMovimiento.AJUSTE, 10, 5, 7) shouldBe
                ResultadoMovimiento.Ok(nuevoStock = 7, variacion = -3, quedoConStockBajo = false)
    }

    "una cantidad negativa da error" {
        calcularMovimiento(TipoMovimiento.INGRESO, 10, 5, -1) shouldBe
                ResultadoMovimiento.Error("La cantidad no puede ser negativa")
    }
})