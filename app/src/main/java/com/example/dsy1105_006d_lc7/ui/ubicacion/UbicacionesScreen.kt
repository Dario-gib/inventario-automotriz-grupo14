package com.example.dsy1105_006d_lc7.ui.ubicacion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.data.repository.ProductoConUbicacion
import com.example.dsy1105_006d_lc7.data.repository.textoUbicacion
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbicacionesScreen(
    viewModel: UbicacionViewModel,
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
                title = { Text("Ubicación en bodega") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = appTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                label = { Text("Buscar por producto, código o ubicación") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (estado.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (estado.busqueda.isBlank()) "No hay productos registrados."
                        else "Sin resultados para la búsqueda"
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(estado.items, key = { it.producto.id }) { item ->
                        ItemUbicacion(item = item, onClick = { viewModel.editar(item) })
                    }
                }
            }
        }
    }

    estado.edicion?.let { form ->
        DialogoUbicacion(form = form, viewModel = viewModel)
    }
}

@Composable
private fun ItemUbicacion(item: ProductoConUbicacion, onClick: () -> Unit) {
    val sinUbicacion = item.ubicacion == null
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (sinUbicacion) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${item.producto.nombre} (${item.producto.codigo})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (sinUbicacion) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(item.textoUbicacion())
            }
        }
    }
}

@Composable
private fun DialogoUbicacion(form: UbicacionFormState, viewModel: UbicacionViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::cancelarEdicion,
        title = { Text("Ubicación: ${form.producto.nombre}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = form.zona,
                    onValueChange = viewModel::onZonaChange,
                    label = { Text("Zona") },
                    isError = form.zonaError != null,
                    supportingText = { Text(form.zonaError ?: "") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = form.estante,
                    onValueChange = viewModel::onEstanteChange,
                    label = { Text("Estante") },
                    isError = form.estanteError != null,
                    supportingText = { Text(form.estanteError ?: "") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = form.repisa,
                    onValueChange = viewModel::onRepisaChange,
                    label = { Text("Repisa") },
                    isError = form.repisaError != null,
                    supportingText = { Text(form.repisaError ?: "") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::guardar) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = viewModel::cancelarEdicion) { Text("Cancelar") }
        }
    )
}