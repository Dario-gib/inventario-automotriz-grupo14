package com.example.dsy1105_006d_lc7.ui.reposicion

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import com.example.dsy1105_006d_lc7.data.repository.ProveedorRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class ReposicionScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val bujias = ProductoEntity(
        id = 1, codigo = "BUJ-001", nombre = "Bujías", categoria = "Encendido",
        cantidad = 2, stockMinimo = 5
    )
    private val parachoques = ProductoEntity(
        id = 2, codigo = "CAR-001", nombre = "Parachoques", categoria = "Carrocería",
        cantidad = 1, stockMinimo = 3
    )
    private val andes = ProveedorEntity(
        id = 1, codigo = "PRV-001", nombre = "Repuestos Andes",
        correo = "ventas@andes.example", categorias = "Motor,Encendido", estado = "Activo"
    )

    private fun mostrarPantalla(productos: List<ProductoEntity>) {
        val productoRepo = mockk<ProductoRepository>()
        every { productoRepo.observarTodos() } returns flowOf(productos)
        val proveedorRepo = mockk<ProveedorRepository>()
        every { proveedorRepo.proveedores } returns flowOf(listOf(andes))
        coEvery { proveedorRepo.sincronizar() } returns true
        val vm = ReposicionViewModel(productoRepo, proveedorRepo)
        composeRule.setContent {
            ReposicionScreen(viewModel = vm, onVerProveedores = {}, onVolver = {})
        }
    }

    private fun esperarTexto(texto: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(texto, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun agrupa_los_productos_con_stock_bajo_bajo_su_proveedor() {
        mostrarPantalla(listOf(bujias))
        esperarTexto("Repuestos Andes")

        composeRule.onNodeWithText("Repuestos Andes").assertIsDisplayed()
        composeRule.onNodeWithText("• Bujías (BUJ-001): disponible 2, solicitar 8").assertIsDisplayed()
        composeRule.onNodeWithText("Preparar correo").assertIsDisplayed()
    }

    @Test
    fun un_producto_sin_proveedor_aparece_aparte() {
        mostrarPantalla(listOf(parachoques))
        esperarTexto("Sin proveedor asignado")

        composeRule.onNodeWithText("Sin proveedor asignado").assertIsDisplayed()
        composeRule.onNodeWithText("• Parachoques (CAR-001) · categoría Carrocería").assertIsDisplayed()
    }

    @Test
    fun sin_productos_con_stock_bajo_no_pide_reposicion() {
        mostrarPantalla(emptyList())
        esperarTexto("No hay productos con stock bajo")

        composeRule.onNodeWithText("No hay productos con stock bajo", substring = true).assertIsDisplayed()
    }
}