package com.example.dsy1105_006d_lc7.ui.proveedor

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import com.example.dsy1105_006d_lc7.data.repository.ProveedorRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class ProveedoresScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val andes = ProveedorEntity(
        id = 1, codigo = "PRV-001", nombre = "Repuestos Andes",
        correo = "ventas@andes.example", categorias = "Motor,Encendido", estado = "Activo"
    )
    private val norte = ProveedorEntity(
        id = 2, codigo = "PRV-003", nombre = "Lubricantes del Norte",
        correo = "contacto@norte.example", categorias = "Lubricantes", estado = "Inactivo"
    )

    // Repositorio simulado: la "API" devuelve estos proveedores
    private fun mostrarPantalla(proveedores: List<ProveedorEntity>, sincroniza: Boolean = true) {
        val repo = mockk<ProveedorRepository>()
        every { repo.proveedores } returns flowOf(proveedores)
        coEvery { repo.sincronizar() } returns sincroniza
        val vm = ProveedorViewModel(repo)
        composeRule.setContent { ProveedoresScreen(viewModel = vm, onVolver = {}) }
    }

    private fun esperarTexto(texto: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(texto, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun muestra_los_proveedores_que_entrega_la_api() {
        mostrarPantalla(listOf(andes, norte))
        esperarTexto("Repuestos Andes")

        composeRule.onNodeWithText("Repuestos Andes").assertIsDisplayed()
        composeRule.onNodeWithText("Código: PRV-001").assertIsDisplayed()
        composeRule.onNodeWithText("Categorías: Motor, Encendido").assertIsDisplayed()
    }

    @Test
    fun marca_a_los_proveedores_inactivos() {
        mostrarPantalla(listOf(andes, norte))
        esperarTexto("Lubricantes del Norte")

        composeRule.onNodeWithText("Inactivo").assertIsDisplayed()
    }

    @Test
    fun sin_conexion_avisa_y_conserva_los_datos_guardados() {
        mostrarPantalla(listOf(andes), sincroniza = false)
        esperarTexto("Sin conexión")

        composeRule.onNodeWithText("Sin conexión: se muestran los datos guardados").assertIsDisplayed()
        composeRule.onNodeWithText("Repuestos Andes").assertIsDisplayed()
    }
}