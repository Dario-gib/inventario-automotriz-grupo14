package com.example.dsy1105_006d_lc7.ui.login

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dsy1105_006d_lc7.data.repository.AuthRepository
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // Navegación mínima de prueba: el login y una pantalla de destino
    private fun mostrarLogin() {
        composeRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    val vm = remember { LoginViewModel(AuthRepository()) }
                    LoginScreen(navController = navController, vm = vm)
                }
                composable(
                    route = "DrawerMenu/{username}",
                    arguments = listOf(navArgument("username") { type = NavType.StringType })
                ) { entry ->
                    Text("Menú de prueba: ${entry.arguments?.getString("username")}")
                }
            }
        }
    }

    private fun iniciarSesion(usuario: String, clave: String) {
        composeRule.onNodeWithText("Usuario").performTextInput(usuario)
        composeRule.onNodeWithText("Contraseña").performTextInput(clave)
        composeRule.onNodeWithText("Iniciar Sesion").performClick()
    }

    @Test
    fun muestra_los_campos_y_el_boton_de_ingreso() {
        mostrarLogin()

        composeRule.onNodeWithText("Usuario").assertIsDisplayed()
        composeRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeRule.onNodeWithText("Iniciar Sesion").assertIsDisplayed()
    }

    @Test
    fun con_credenciales_incorrectas_muestra_el_error() {
        mostrarLogin()

        iniciarSesion("admin", "999")

        composeRule.onNodeWithText("Credenciales inválidas").assertIsDisplayed()
    }

    @Test
    fun con_credenciales_correctas_navega_al_menu() {
        mostrarLogin()

        iniciarSesion("admin", "123")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Menú de prueba: admin").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Menú de prueba: admin").assertIsDisplayed()
    }
}