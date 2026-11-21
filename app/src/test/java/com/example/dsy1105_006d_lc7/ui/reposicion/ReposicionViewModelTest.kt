package com.example.dsy1105_006d_lc7.ui.reposicion

import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import com.example.dsy1105_006d_lc7.data.repository.GrupoReposicion
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import com.example.dsy1105_006d_lc7.data.repository.ProveedorRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
class ReposicionViewModelTest : StringSpec({

    val dispatcher = UnconfinedTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val bujias = ProductoEntity(
        id = 1, codigo = "BUJ-001", nombre = "Bujías", categoria = "Encendido",
        cantidad = 2, stockMinimo = 5
    )
    val andes = ProveedorEntity(
        id = 1, codigo = "PRV-001", nombre = "Repuestos Andes",
        correo = "ventas@andes.example", categorias = "Motor,Encendido", estado = "Activo"
    )

    fun repos(sincroniza: Boolean): Pair<ProductoRepository, ProveedorRepository> {
        val productos = mockk<ProductoRepository>()
        every { productos.observarTodos() } returns flowOf(listOf(bujias))
        val proveedores = mockk<ProveedorRepository>()
        every { proveedores.proveedores } returns flowOf(listOf(andes))
        coEvery { proveedores.sincronizar() } returns sincroniza
        return productos to proveedores
    }

    "el estado agrupa los productos con stock bajo por proveedor" {
        runTest(dispatcher) {
            val (productos, proveedores) = repos(sincroniza = true)

            val vm = ReposicionViewModel(productos, proveedores)

            vm.estado.value.plan.grupos.size shouldBe 1
            vm.estado.value.plan.grupos[0].proveedor shouldBe andes
            vm.estado.value.plan.grupos[0].productos shouldBe listOf(bujias)
            vm.estado.value.mensaje shouldBe "Proveedores actualizados desde la API"
            vm.estado.value.cargando shouldBe false
        }
    }

    "sin conexión avisa y conserva el plan con los datos guardados" {
        runTest(dispatcher) {
            val (productos, proveedores) = repos(sincroniza = false)

            val vm = ReposicionViewModel(productos, proveedores)

            vm.estado.value.plan.grupos.size shouldBe 1
            vm.estado.value.mensaje shouldBe "Sin conexión: se muestran los datos guardados"
        }
    }

    "el borrador usa el correo del proveedor y lista sus productos" {
        runTest(dispatcher) {
            val (productos, proveedores) = repos(sincroniza = true)
            val vm = ReposicionViewModel(productos, proveedores)

            val borrador = vm.generarBorrador(GrupoReposicion(andes, listOf(bujias)))

            borrador.destinatario shouldBe "ventas@andes.example"
            borrador.cuerpo shouldContain "Repuestos Andes"
            borrador.cuerpo shouldContain "BUJ-001"
            borrador.cuerpo shouldContain "solicitar 8 unidades"
        }
    }
})