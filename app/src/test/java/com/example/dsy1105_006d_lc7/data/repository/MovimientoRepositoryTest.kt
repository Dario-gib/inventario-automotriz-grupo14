package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.MovimientoDao
import com.example.dsy1105_006d_lc7.data.local.ProductoDao
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class MovimientoRepositoryTest : StringSpec({

    val frenos = ProductoEntity(
        id = 1, codigo = "FRE-001", nombre = "Pastillas de freno",
        categoria = "Frenos", cantidad = 10, stockMinimo = 5
    )

    fun daos(producto: ProductoEntity?): Pair<ProductoDao, MovimientoDao> {
        val productoDao = mockk<ProductoDao>()
        coEvery { productoDao.obtenerPorId(1) } returns producto
        val movimientoDao = mockk<MovimientoDao>()
        every { movimientoDao.observarTodos() } returns flowOf(emptyList())
        coEvery { movimientoDao.registrarMovimiento(any(), any()) } returns Unit
        return productoDao to movimientoDao
    }

    "una venta válida guarda el movimiento y el nuevo stock" {
        val (productoDao, movimientoDao) = daos(frenos)
        val repo = MovimientoRepository(productoDao, movimientoDao)

        val resultado = repo.registrar(1, TipoMovimiento.VENTA, 4, "Cliente mostrador")

        resultado shouldBe ResultadoMovimiento.Ok(nuevoStock = 6, variacion = -4, quedoConStockBajo = false)
        coVerify(exactly = 1) {
            movimientoDao.registrarMovimiento(
                match {
                    it.productoId == 1 && it.tipo == "VENTA" &&
                            it.cantidad == -4 && it.nota == "Cliente mostrador"
                },
                6
            )
        }
    }

    "una venta sin stock suficiente no guarda nada" {
        val (productoDao, movimientoDao) = daos(frenos)
        val repo = MovimientoRepository(productoDao, movimientoDao)

        val resultado = repo.registrar(1, TipoMovimiento.VENTA, 50, "")

        resultado shouldBe ResultadoMovimiento.Error("Stock insuficiente: hay 10 unidades disponibles")
        coVerify(exactly = 0) { movimientoDao.registrarMovimiento(any(), any()) }
    }

    "si el producto no existe devuelve error y no guarda" {
        val (productoDao, movimientoDao) = daos(null)
        val repo = MovimientoRepository(productoDao, movimientoDao)

        val resultado = repo.registrar(1, TipoMovimiento.INGRESO, 5, "")

        resultado shouldBe ResultadoMovimiento.Error("El producto ya no existe")
        coVerify(exactly = 0) { movimientoDao.registrarMovimiento(any(), any()) }
    }

    "un ajuste guarda la variación con signo" {
        val (productoDao, movimientoDao) = daos(frenos)
        val repo = MovimientoRepository(productoDao, movimientoDao)

        repo.registrar(1, TipoMovimiento.AJUSTE, 7, "Conteo de bodega")

        coVerify(exactly = 1) {
            movimientoDao.registrarMovimiento(
                match { it.tipo == "AJUSTE" && it.cantidad == -3 },
                7
            )
        }
    }
})