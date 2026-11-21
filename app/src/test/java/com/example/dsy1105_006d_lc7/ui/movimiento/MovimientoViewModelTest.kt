package com.example.dsy1105_006d_lc7.ui.movimiento

import com.example.dsy1105_006d_lc7.data.local.MovimientoEntity
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.repository.MovimientoRepository
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import com.example.dsy1105_006d_lc7.data.repository.ResultadoMovimiento
import com.example.dsy1105_006d_lc7.data.repository.TipoMovimiento
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MovimientoViewModelTest : StringSpec({

    val dispatcher = UnconfinedTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val frenos = ProductoEntity(
        id = 1, codigo = "FRE-001", nombre = "Pastillas de freno",
        categoria = "Frenos", cantidad = 10, stockMinimo = 5
    )

    fun repos(
        movimientos: List<MovimientoEntity> = emptyList()
    ): Pair<ProductoRepository, MovimientoRepository> {
        val productos = mockk<ProductoRepository>()
        every { productos.observarTodos() } returns flowOf(listOf(frenos))
        val movs = mockk<MovimientoRepository>()
        every { movs.historial } returns flowOf(movimientos)
        return productos to movs
    }

    "registrar sin producto ni cantidad muestra errores y no guarda" {
        runTest(dispatcher) {
            val (productos, movs) = repos()
            val vm = MovimientoViewModel(productos, movs)

            vm.registrar()

            vm.estado.value.form.productoError shouldBe "Selecciona un producto"
            vm.estado.value.form.cantidadError shouldBe "Campo obligatorio"
            coVerify(exactly = 0) { movs.registrar(any(), any(), any(), any()) }
        }
    }

    "una cantidad con texto muestra error" {
        runTest(dispatcher) {
            val (productos, movs) = repos()
            val vm = MovimientoViewModel(productos, movs)
            vm.onProductoSeleccionado(1)
            vm.onCantidadChange("abc")

            vm.registrar()

            vm.estado.value.form.cantidadError shouldBe "Debe ser un número entero"
            coVerify(exactly = 0) { movs.registrar(any(), any(), any(), any()) }
        }
    }

    "una venta válida se registra y muestra el stock actual" {
        runTest(dispatcher) {
            val (productos, movs) = repos()
            coEvery { movs.registrar(1, TipoMovimiento.VENTA, 4, "") } returns
                    ResultadoMovimiento.Ok(nuevoStock = 6, variacion = -4, quedoConStockBajo = false)
            val vm = MovimientoViewModel(productos, movs)
            vm.onProductoSeleccionado(1)
            vm.onTipoChange(TipoMovimiento.VENTA)
            vm.onCantidadChange("4")

            vm.registrar()

            vm.estado.value.mensaje shouldBe "Movimiento registrado (Venta). Stock actual: 6."
            vm.estado.value.form.cantidad shouldBe ""
            coVerify(exactly = 1) { movs.registrar(1, TipoMovimiento.VENTA, 4, "") }
        }
    }

    "el error de stock insuficiente se muestra en el campo cantidad" {
        runTest(dispatcher) {
            val (productos, movs) = repos()
            coEvery { movs.registrar(any(), any(), any(), any()) } returns
                    ResultadoMovimiento.Error("Stock insuficiente: hay 10 unidades disponibles")
            val vm = MovimientoViewModel(productos, movs)
            vm.onProductoSeleccionado(1)
            vm.onTipoChange(TipoMovimiento.VENTA)
            vm.onCantidadChange("50")

            vm.registrar()

            vm.estado.value.form.cantidadError shouldBe "Stock insuficiente: hay 10 unidades disponibles"
            vm.estado.value.mensaje shouldBe null
        }
    }

    "avisa cuando el producto queda con stock bajo" {
        runTest(dispatcher) {
            val (productos, movs) = repos()
            coEvery { movs.registrar(any(), any(), any(), any()) } returns
                    ResultadoMovimiento.Ok(nuevoStock = 3, variacion = -7, quedoConStockBajo = true)
            val vm = MovimientoViewModel(productos, movs)
            vm.onProductoSeleccionado(1)
            vm.onTipoChange(TipoMovimiento.CONSUMO)
            vm.onCantidadChange("7")

            vm.registrar()

            vm.estado.value.mensaje!! shouldContain "stock bajo"
        }
    }

    "el historial muestra el nombre del producto" {
        runTest(dispatcher) {
            val mov = MovimientoEntity(id = 1, productoId = 1, tipo = "VENTA", cantidad = -4)
            val (productos, movs) = repos(listOf(mov))

            val vm = MovimientoViewModel(productos, movs)

            vm.estado.value.historial.size shouldBe 1
            vm.estado.value.historial[0].producto shouldBe "Pastillas de freno (FRE-001)"
        }
    }
})