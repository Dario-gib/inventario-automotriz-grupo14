package com.example.dsy1105_006d_lc7.ui.producto

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductoFormScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun viewModelVacio(): ProductoViewModel {
        val repo = mockk<ProductoRepository>()
        every { repo.observarTodos() } returns flowOf(emptyList())
        every { repo.buscar(any()) } returns flowOf(emptyList())
        coEvery { repo.obtenerPorCodigo(any()) } returns null
        coEvery { repo.insertar(any()) } returns Unit
        return ProductoViewModel(repo)
    }

    private fun mostrarFormulario(onVolver: () -> Unit = {}) {
        val vm = viewModelVacio()
        composeRule.setContent {
            ProductoFormScreen(viewModel = vm, onEscanearCodigo = {}, onVolver = onVolver)
        }
    }

    private fun escribir(campo: String, texto: String) {
        composeRule.onNodeWithText(campo).performScrollTo().performTextInput(texto)
    }

    private fun esperarTexto(texto: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun muestra_el_titulo_y_los_campos_del_formulario() {
        mostrarFormulario()

        composeRule.onNodeWithText("Nuevo producto").assertIsDisplayed()
        composeRule.onNodeWithText("Código").assertExists()
        composeRule.onNodeWithText("Nombre").assertExists()
        composeRule.onNodeWithText("Categoría").assertExists()
        composeRule.onNodeWithText("Cantidad").assertExists()
        composeRule.onNodeWithText("Stock mínimo").assertExists()
    }

    @Test
    fun guardar_con_campos_vacios_muestra_los_errores() {
        mostrarFormulario()

        composeRule.onNodeWithText("Guardar").performScrollTo().performClick()

        esperarTexto("El código es obligatorio")
        composeRule.onNodeWithText("El código es obligatorio").assertExists()
        composeRule.onNodeWithText("El nombre es obligatorio").assertExists()
        composeRule.onNodeWithText("La categoría es obligatoria").assertExists()
        assertTrue(composeRule.onAllNodesWithText("Campo obligatorio").fetchSemanticsNodes().size >= 2)
    }

    @Test
    fun escribir_en_un_campo_limpia_su_error() {
        mostrarFormulario()
        composeRule.onNodeWithText("Guardar").performScrollTo().performClick()
        esperarTexto("El nombre es obligatorio")

        escribir("Nombre", "Filtro de aceite")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("El nombre es obligatorio").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun guardar_un_producto_valido_vuelve_a_la_lista() {
        var volvio = false
        mostrarFormulario(onVolver = { volvio = true })

        escribir("Código", "FRE-001")
        escribir("Nombre", "Pastillas de freno")
        escribir("Categoría", "Frenos")
        escribir("Cantidad", "10")
        escribir("Stock mínimo", "5")
        composeRule.onNodeWithText("Guardar").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { volvio }
        assertTrue(volvio)
    }
}