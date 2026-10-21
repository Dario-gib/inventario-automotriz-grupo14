package com.example.dsy1105_006d_lc7.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    username: String,
    navController: NavController
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Menú principal") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Hola, $username", style = MaterialTheme.typography.headlineSmall)

            NavigationDrawerItem(
                label = { Text("Productos") },
                selected = false,
                onClick = { navController.navigate("lista_productos") },
                icon = { Icon(Icons.Default.Inventory2, contentDescription = "Productos") }
            )

            NavigationDrawerItem(
                label = { Text("Cerrar sesión") },
                selected = false,
                onClick = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir") }
            )
        }
    }
}

