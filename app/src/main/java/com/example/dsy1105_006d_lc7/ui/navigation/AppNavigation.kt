package com.example.dsy1105_006d_lc7.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dsy1105_006d_lc7.ui.login.LoginScreen
import com.example.dsy1105_006d_lc7.ui.menu.MenuScreen
import com.example.dsy1105_006d_lc7.ui.producto.ListaProductosScreen
import com.example.dsy1105_006d_lc7.ui.producto.ProductoFormScreen
import com.example.dsy1105_006d_lc7.ui.producto.ProductoViewModel
import com.example.dsy1105_006d_lc7.ui.qr.QrScannerScreen
import com.example.dsy1105_006d_lc7.ui.qr.QrViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Un solo ViewModel compartido entre la lista y el formulario
    val productoViewModel: ProductoViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable(
            route = "DrawerMenu/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username").orEmpty()
            MenuScreen(username = username, navController = navController)
        }

        composable("lista_productos") {
            ListaProductosScreen(
                viewModel = productoViewModel,
                onAgregar = {
                    productoViewModel.nuevoProducto()
                    navController.navigate("producto_form/0")
                },
                onEditar = { id ->
                    productoViewModel.cargarProducto(id)
                    navController.navigate("producto_form/$id")
                },
                onEscanearQr = { navController.navigate("qr_scanner") },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = "producto_form/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) {
            ProductoFormScreen(
                viewModel = productoViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable("qr_scanner") {
            val qrViewModel: QrViewModel = viewModel()
            QrScannerScreen(
                viewModel = qrViewModel,
                onVolver = { navController.popBackStack() },
                onEditarProducto = { id ->
                    productoViewModel.cargarProducto(id)
                    navController.navigate("producto_form/$id")
                },
                onRegistrarProducto = { codigo ->
                    productoViewModel.nuevoProducto()
                    productoViewModel.onCodigoChange(codigo)
                    navController.navigate("producto_form/0")
                }
            )
        }
    }
}