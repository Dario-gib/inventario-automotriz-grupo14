package com.example.dsy1105_006d_lc7.ui.proveedor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import com.example.dsy1105_006d_lc7.data.repository.activo
import com.example.dsy1105_006d_lc7.data.repository.listaCategorias
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedoresScreen(
    viewModel: ProveedorViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proveedores") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::sincronizar) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar desde la API")
                    }
                },
                colors = appTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (estado.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (estado.proveedores.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (estado.cargando) "Consultando la API..."
                        else "Sin proveedores.\nConéctate a Internet y pulsa actualizar."
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(estado.proveedores, key = { it.id }) { ProveedorItem(it) }
                }
            }
        }
    }
}

@Composable
private fun ProveedorItem(proveedor: ProveedorEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(proveedor.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Código: ${proveedor.codigo}")
            Text("Correo: ${proveedor.correo}")
            if (proveedor.telefono.isNotBlank()) Text("Teléfono: ${proveedor.telefono}")
            Text("Categorías: ${proveedor.listaCategorias.joinToString(", ")}")
            Text(
                proveedor.estado,
                fontWeight = FontWeight.Bold,
                color = if (proveedor.activo) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    }
}