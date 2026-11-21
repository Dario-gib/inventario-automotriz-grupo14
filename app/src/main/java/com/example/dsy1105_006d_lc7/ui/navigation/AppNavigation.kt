package com.example.dsy1105_006d_lc7.ui.navigation

import android.app.Application
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
import com.example.dsy1105_006d_lc7.ui.producto.ProductoViewModelFactory
import com.example.dsy1105_006d_lc7.ui.qr.QrCapturaScreen
import com.example.dsy1105_006d_lc7.ui.qr.QrScannerScreen
import com.example.dsy1105_006d_lc7.ui.qr.QrViewModel
import com.example.dsy1105_006d_lc7.ui.qr.QrViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Un solo ViewModel compartido entre la lista y el formulario
    val context = LocalContext.current
    val productoViewModel: ProductoViewModel = viewModel(
        factory = ProductoViewModelFactory(context.applicationContext as Application)
    )

    // Transiciones: las pantallas entran deslizándose y vuelven en sentido contrario
    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            )
        }
    ) {

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
                onEscanearCodigo = { navController.navigate("qr_codigo") },
                onVolver = { navController.popBackStack() }
            )
        }

        composable("qr_scanner") {
            val qrViewModel: QrViewModel = viewModel(
                factory = QrViewModelFactory(context.applicationContext as Application)
            )
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

        composable("qr_codigo") {
            QrCapturaScreen(
                onCodigoLeido = { codigo ->
                    productoViewModel.onCodigoChange(codigo)
                    navController.popBackStack()
                },
                onVolver = { navController.popBackStack() }
            )
        }
    } // fin NavHost
} // fin AppNavigation