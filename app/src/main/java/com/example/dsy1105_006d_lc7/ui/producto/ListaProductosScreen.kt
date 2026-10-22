package com.example.dsy1105_006d_lc7.ui.producto

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyRow
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.stockBajo
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaProductosScreen(
    viewModel: ProductoViewModel,
    onAgregar: () -> Unit,
    onEditar: (Int) -> Unit,
    onEscanearQr: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.lista.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    var productoAEliminar by remember { mutableStateOf<ProductoEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Mensaje que llega desde el formulario (producto creado o actualizado)
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEscanearQr) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
                    }
                },
                colors = appTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregar) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Resumen del inventario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResumenCard(
                    titulo = "Total",
                    valor = estado.total,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                ResumenCard(
                    titulo = "Stock bajo",
                    valor = estado.stockBajo,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                label = { Text("Buscar por nombre o código") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            FilterChip(
                selected = estado.soloStockBajo,
                onClick = { viewModel.onSoloStockBajoChange(!estado.soloStockBajo) },
                label = { Text("Solo stock bajo") },
                leadingIcon = {
                    Icon(
                        if (estado.soloStockBajo) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null
                    )
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (estado.categorias.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = estado.categoriaSeleccionada == null,
                            onClick = { viewModel.onCategoriaSeleccionada(null) },
                            label = { Text("Todas") }
                        )
                    }
                    items(estado.categorias) { categoria ->
                        FilterChip(
                            selected = estado.categoriaSeleccionada.equals(categoria, ignoreCase = true),
                            onClick = { viewModel.onCategoriaSeleccionada(categoria) },
                            label = { Text(categoria) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (estado.productos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        when {
                            estado.busqueda.isNotBlank() -> "Sin resultados para la búsqueda"
                            estado.soloStockBajo -> "No hay productos con stock bajo"
                            estado.categoriaSeleccionada != null -> "No hay productos en esta categoría"
                            else -> "Aún no hay productos.\nPresiona + para agregar uno."
                        }
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(estado.productos, key = { it.id }) { producto ->
                        ProductoItem(
                            producto = producto,
                            onEditar = { onEditar(producto.id) },
                            onEliminar = { productoAEliminar = producto }
                        )
                    }
                }
            }
        }
    }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que quieres eliminar \"${producto.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(producto)
                    productoAEliminar = null
                    scope.launch {
                        val resultado = snackbarHostState.showSnackbar(
                            message = "Producto eliminado",
                            actionLabel = "Deshacer",
                            duration = SnackbarDuration.Long
                        )
                        if (resultado == SnackbarResult.ActionPerformed) {
                            viewModel.restaurar(producto)
                        }
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ResumenCard(
    titulo: String,
    valor: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge)
            Text(
                valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProductoItem(
    producto: ProductoEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val bajo = producto.stockBajo

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditar() },
        colors = CardDefaults.cardColors(
            containerColor = if (bajo) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Código: ${producto.codigo}")
                Text("Categoría: ${producto.categoria}")
                Text("Stock: ${producto.cantidad} (mínimo ${producto.stockMinimo})")
                if (bajo) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Stock bajo",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar producto")
            }
        }
    }
}