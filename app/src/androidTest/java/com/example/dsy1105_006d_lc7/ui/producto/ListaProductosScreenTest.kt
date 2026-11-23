package com.example.dsy1105_006d_lc7.ui.producto

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.UbicacionEntity
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ListaProductosScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val pastillas = ProductoEntity(
        id = 1, codigo = "FRE-001", nombre = "Pastillas de freno",
        categoria = "Frenos", cantidad = 10, stockMinimo = 5
    )
    private val bujias = ProductoEntity(
        id = 2, codigo = "BUJ-001", nombre = "Bujías",
        categoria = "Encendido", cantidad = 2, stockMinimo = 5
    )

    // ViewModel real con un repositorio simulado
    private fun viewModelCon(productos: List<ProductoEntity>): ProductoViewModel {
        val repo = mockk<ProductoRepository>()
        every { repo.observarTodos() } returns flowOf(productos)
        every { repo.buscar(any()) } returns flowOf(productos)
        return ProductoViewModel(repo)
    }

    private fun mostrarLista(
        productos: List<ProductoEntity>,
        onEditar: (Int) -> Unit = {}
    ) {
        val vm = viewModelCon(productos)
        composeRule.setContent {
            ListaProductosScreen(
                viewModel = vm,
                onAgregar = {},
                onEditar = onEditar,
                onEscanearQr = {},
                onVolver = {}
            )
        }
    }

    private fun esperarTexto(texto: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(texto, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun muestra_los_productos_guardados() {
        mostrarLista(listOf(pastillas, bujias))
        esperarTexto("Pastillas de freno")

        composeRule.onNodeWithText("Pastillas de freno").assertIsDisplayed()
        composeRule.onNodeWithText("Bujías").assertIsDisplayed()
        composeRule.onNodeWithText("Código: FRE-001").assertIsDisplayed()
    }

    @Test
    fun muestra_el_resumen_del_inventario() {
        mostrarLista(listOf(pastillas, bujias))
        esperarTexto("Pastillas de freno")

        composeRule.onNodeWithText("Total").assertIsDisplayed()
    }

    @Test
    fun marca_el_producto_con_stock_bajo() {
        mostrarLista(listOf(pastillas, bujias))
        esperarTexto("Bujías")

        // Una vez en el resumen y otra en la tarjeta del producto
        val nodos = composeRule.onAllNodesWithText("Stock bajo").fetchSemanticsNodes()
        assertTrue(nodos.size >= 2)
    }

    @Test
    fun al_tocar_un_producto_se_pide_editarlo() {
        var idEditado: Int? = null
        mostrarLista(listOf(pastillas, bujias), onEditar = { idEditado = it })
        esperarTexto("Pastillas de freno")

        composeRule.onNodeWithText("Código: FRE-001").performClick()

        assertEquals(1, idEditado)
    }

    @Test
    fun sin_productos_muestra_el_mensaje_vacio() {
        mostrarLista(emptyList())
        esperarTexto("Aún no hay productos")

        composeRule.onNodeWithText("Aún no hay productos", substring = true).assertIsDisplayed()
    }

    @Test
    fun muestra_la_marca_y_la_ubicacion_del_producto() {
        val conMarca = pastillas.copy(marca = "Brembo")
        val repo = mockk<ProductoRepository>()
        every { repo.observarTodos() } returns flowOf(listOf(conMarca))
        every { repo.buscar(any()) } returns flowOf(listOf(conMarca))
        val ubicacion = UbicacionEntity(productoId = 1, zona = "A", estante = "3", repisa = "2")
        val vm = ProductoViewModel(repo, flowOf(listOf(ubicacion)))
        composeRule.setContent {
            ListaProductosScreen(
                viewModel = vm,
                onAgregar = {},
                onEditar = {},
                onEscanearQr = {},
                onVolver = {}
            )
        }
        esperarTexto("Pastillas de freno")

        composeRule.onNodeWithText("Marca: Brembo").assertIsDisplayed()
        composeRule.onNodeWithText("Zona A · Estante 3 · Repisa 2").assertIsDisplayed()
    }

}