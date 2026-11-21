package com.example.dsy1105_006d_lc7.ui.proveedor

import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import com.example.dsy1105_006d_lc7.data.repository.ProveedorRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
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
class ProveedorViewModelTest : StringSpec({

    val dispatcher = UnconfinedTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val andes = ProveedorEntity(
        id = 1, codigo = "PRV-001", nombre = "Repuestos Andes",
        correo = "ventas@andes.example", categorias = "Motor,Encendido", estado = "Activo"
    )

    "muestra los proveedores y avisa que se actualizaron desde la API" {
        runTest(dispatcher) {
            val repo = mockk<ProveedorRepository>()
            every { repo.proveedores } returns flowOf(listOf(andes))
            coEvery { repo.sincronizar() } returns true

            val vm = ProveedorViewModel(repo)

            vm.estado.value.proveedores shouldBe listOf(andes)
            vm.estado.value.cargando shouldBe false
            vm.estado.value.mensaje shouldBe "Proveedores actualizados desde la API"
        }
    }

    "sin conexión avisa y conserva los datos guardados" {
        runTest(dispatcher) {
            val repo = mockk<ProveedorRepository>()
            every { repo.proveedores } returns flowOf(listOf(andes))
            coEvery { repo.sincronizar() } returns false

            val vm = ProveedorViewModel(repo)

            vm.estado.value.proveedores shouldBe listOf(andes)
            vm.estado.value.mensaje shouldBe "Sin conexión: se muestran los datos guardados"
        }
    }

    "mensajeMostrado limpia el mensaje" {
        runTest(dispatcher) {
            val repo = mockk<ProveedorRepository>()
            every { repo.proveedores } returns flowOf(emptyList())
            coEvery { repo.sincronizar() } returns true
            val vm = ProveedorViewModel(repo)

            vm.mensajeMostrado()

            vm.estado.value.mensaje shouldBe null
        }
    }
})