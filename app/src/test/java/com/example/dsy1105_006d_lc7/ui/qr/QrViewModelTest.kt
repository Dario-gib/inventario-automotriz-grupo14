package com.example.dsy1105_006d_lc7.ui.qr

import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class QrViewModelTest : StringSpec({

    val dispatcher = UnconfinedTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val frenos = ProductoEntity(
        id = 1, codigo = "FRE-001", nombre = "Pastillas de freno",
        categoria = "Frenos", cantidad = 10, stockMinimo = 5
    )

    "al leer el QR de un producto registrado se muestra el producto" {
        runTest(dispatcher) {
            val repo = mockk<ProductoRepository>()
            coEvery { repo.obtenerPorCodigo("FRE-001") } returns frenos
            val vm = QrViewModel(repo)

            vm.onQrDetected(" FRE-001 ")

            vm.uiState.value.codigoLeido shouldBe "FRE-001"
            vm.uiState.value.producto shouldBe frenos
            vm.uiState.value.buscando shouldBe false
        }
    }

    "al leer un código que no existe el producto queda vacío" {
        runTest(dispatcher) {
            val repo = mockk<ProductoRepository>()
            coEvery { repo.obtenerPorCodigo("XXX-999") } returns null
            val vm = QrViewModel(repo)

            vm.onQrDetected("XXX-999")

            vm.uiState.value.codigoLeido shouldBe "XXX-999"
            vm.uiState.value.producto shouldBe null
        }
    }

    "limpiar deja el estado listo para un nuevo escaneo" {
        runTest(dispatcher) {
            val repo = mockk<ProductoRepository>()
            coEvery { repo.obtenerPorCodigo(any()) } returns frenos
            val vm = QrViewModel(repo)
            vm.onQrDetected("FRE-001")

            vm.limpiar()

            vm.uiState.value shouldBe QrUiState()
        }
    }
})