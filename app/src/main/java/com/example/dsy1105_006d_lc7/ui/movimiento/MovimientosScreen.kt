package com.example.dsy1105_006d_lc7.ui.movimiento

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.data.repository.TipoMovimiento
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreen(
    viewModel: MovimientoViewModel,
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
                title = { Text("Movimientos") },
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
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { FormularioMovimiento(estado, viewModel) }
            item {
                Text("Historial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (estado.historial.isEmpty()) {
                item { Text("Aún no hay movimientos registrados.") }
            } else {
                items(estado.historial, key = { it.movimiento.id }) { MovimientoItem(it) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FormularioMovimiento(estado: MovimientosUiState, viewModel: MovimientoViewModel) {
    val form = estado.form
    var expandido by remember { mutableStateOf(false) }
    val seleccionado = estado.productos.firstOrNull { it.id == form.productoId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Registrar movimiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                OutlinedTextField(
                    value = seleccionado?.let { "${it.codigo} · ${it.nombre} (stock ${it.cantidad})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Producto") },
                    isError = form.productoError != null,
                    supportingText = { Text(form.productoError ?: "") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                    estado.productos.forEach { p ->
                        DropdownMenuItem(
                            text = { Text("${p.codigo} · ${p.nombre} (stock ${p.cantidad})") },
                            onClick = {
                                viewModel.onProductoSeleccionado(p.id)
                                expandido = false
                            }
                        )
                    }
                }
            }

            Text("Tipo de movimiento", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoMovimiento.entries.forEach { tipo ->
                    FilterChip(
                        selected = form.tipo == tipo,
                        onClick = { viewModel.onTipoChange(tipo) },
                        label = { Text(tipo.etiqueta) }
                    )
                }
            }
            Text(form.tipo.descripcion, style = MaterialTheme.typography.bodySmall)

            OutlinedTextField(
                value = form.cantidad,
                onValueChange = viewModel::onCantidadChange,
                label = {
                    Text(if (form.tipo == TipoMovimiento.AJUSTE) "Cantidad real en bodega" else "Cantidad")
                },
                isError = form.cantidadError != null,
                supportingText = { Text(form.cantidadError ?: "") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.nota,
                onValueChange = viewModel::onNotaChange,
                label = { Text("Nota (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = viewModel::registrar, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar movimiento")
            }
        }
    }
}

@Composable
private fun MovimientoItem(detalle: MovimientoDetalle) {
    val m = detalle.movimiento
    val formato = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")) }
    val tipo = TipoMovimiento.entries.firstOrNull { it.name == m.tipo }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${tipo?.etiqueta ?: m.tipo} · ${detalle.producto}",
                    fontWeight = FontWeight.Bold
                )
                Text(formato.format(Date(m.fecha)), style = MaterialTheme.typography.bodySmall)
                if (m.nota.isNotBlank()) Text(m.nota, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                if (m.cantidad > 0) "+${m.cantidad}" else m.cantidad.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (m.cantidad >= 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}