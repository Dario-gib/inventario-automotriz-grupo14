package com.example.dsy1105_006d_lc7.ui.ubicacion

import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.UbicacionEntity
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import com.example.dsy1105_006d_lc7.data.repository.UbicacionRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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
class UbicacionViewModelTest : StringSpec({

    val dispatcher = UnconfinedTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val bujias = ProductoEntity(
        id = 1, codigo = "BUJ-001", nombre = "Bujías", categoria = "Encendido",
        cantidad = 10, stockMinimo = 5
    )
    val bateria = ProductoEntity(
        id = 2, codigo = "BAT-001", nombre = "Batería", categoria = "Baterías",
        cantidad = 10, stockMinimo = 5
    )
    val ubBujias = UbicacionEntity(productoId = 1, zona = "A", estante = "3", repisa = "2")

    fun repos(): Pair<ProductoRepository, UbicacionRepository> {
        val productos = mockk<ProductoRepository>()
        every { productos.observarTodos() } returns flowOf(listOf(bujias, bateria))
        val ubicaciones = mockk<UbicacionRepository>()
        every { ubicaciones.ubicaciones } returns flowOf(listOf(ubBujias))
        coEvery { ubicaciones.guardar(any()) } returns Unit
        return productos to ubicaciones
    }

    "el estado combina los productos con sus ubicaciones" {
        runTest(dispatcher) {
            val (productos, ubicaciones) = repos()

            val vm = UbicacionViewModel(productos, ubicaciones)

            vm.estado.value.todos.size shouldBe 2
            vm.estado.value.todos[0].ubicacion shouldBe ubBujias
            vm.estado.value.todos[1].ubicacion shouldBe null
        }
    }

    "la búsqueda filtra los productos" {
        runTest(dispatcher) {
            val (productos, ubicaciones) = repos()
            val vm = UbicacionViewModel(productos, ubicaciones)

            vm.onBusquedaChange("sin ubicacion")

            vm.estado.value.items.map { it.producto.codigo } shouldBe listOf("BAT-001")
        }
    }

    "editar abre el formulario con la ubicación actual" {
        runTest(dispatcher) {
            val (productos, ubicaciones) = repos()
            val vm = UbicacionViewModel(productos, ubicaciones)

            vm.editar(vm.estado.value.todos[0])

            vm.estado.value.edicion?.zona shouldBe "A"
            vm.estado.value.edicion?.estante shouldBe "3"
            vm.estado.value.edicion?.repisa shouldBe "2"
        }
    }

    "guardar con campos vacíos muestra los errores y no guarda" {
        runTest(dispatcher) {
            val (productos, ubicaciones) = repos()
            val vm = UbicacionViewModel(productos, ubicaciones)
            vm.editar(vm.estado.value.todos[1])   // la batería no tiene ubicación

            vm.guardar()

            val form = vm.estado.value.edicion!!
            form.zonaError shouldBe "La zona es obligatoria"
            form.estanteError shouldBe "El estante es obligatorio"
            form.repisaError shouldBe "La repisa es obligatoria"
            coVerify(exactly = 0) { ubicaciones.guardar(any()) }
        }
    }

    "guardar una ubicación válida la guarda, cierra el formulario y avisa" {
        runTest(dispatcher) {
            val (productos, ubicaciones) = repos()
            val vm = UbicacionViewModel(productos, ubicaciones)
            vm.editar(vm.estado.value.todos[1])
            vm.onZonaChange(" B ")
            vm.onEstanteChange("1")
            vm.onRepisaChange("4")

            vm.guardar()

            coVerify(exactly = 1) {
                ubicaciones.guardar(UbicacionEntity(productoId = 2, zona = "B", estante = "1", repisa = "4"))
            }
            vm.estado.value.edicion shouldBe null
            vm.estado.value.mensaje shouldBe "Ubicación de Batería actualizada"
        }
    }

    "cancelar cierra el formulario sin guardar" {
        runTest(dispatcher) {
            val (productos, ubicaciones) = repos()
            val vm = UbicacionViewModel(productos, ubicaciones)
            vm.editar(vm.estado.value.todos[0])

            vm.cancelarEdicion()

            vm.estado.value.edicion shouldBe null
            coVerify(exactly = 0) { ubicaciones.guardar(any()) }
        }
    }
})