package com.example.dsy1105_006d_lc7.ui.producto

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyRow
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.stockBajo
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors
import com.example.dsy1105_006d_lc7.utils.CorreoHelper
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
    val productosBajos by viewModel.productosStockBajo.collectAsState()
    var productoAEliminar by remember { mutableStateOf<ProductoEntity?>(null) }
    var mostrarSolicitud by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                    // Solo se habilita si hay productos que reponer
                    IconButton(
                        onClick = { mostrarSolicitud = true },
                        enabled = productosBajos.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Solicitar reposición por correo")
                    }
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
                            modifier = Modifier.animateItem(),
                            producto = producto,
                            onEditar = { onEditar(producto.id) },
                            onEliminar = { productoAEliminar = producto }
                        )
                    }
                }
            }
        }
    }

    if (mostrarSolicitud) {
        SolicitudReposicionDialog(
            borradorInicial = remember { viewModel.generarBorrador(productosBajos) },
            validarDestinatario = viewModel::validarDestinatario,
            onCancelar = { mostrarSolicitud = false },
            onConfirmar = { borrador ->
                mostrarSolicitud = false
                val abierto = CorreoHelper.abrirBorrador(
                    context, borrador.destinatario, borrador.asunto, borrador.cuerpo
                )
                if (!abierto) {
                    scope.launch {
                        snackbarHostState.showSnackbar("No hay una aplicación de correo instalada")
                    }
                }
            }
        )
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
    // El número cambia de forma animada cuando se agrega o elimina un producto
    val valorAnimado by animateIntAsState(targetValue = valor, label = "contador")

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge)
            Text(
                valorAnimado.toString(),
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
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bajo = producto.stockBajo

    // El color de la tarjeta cambia con una transición cuando el stock pasa a bajo (o se recupera)
    val colorTarjeta by animateColorAsState(
        targetValue = if (bajo) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "colorTarjeta"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEditar() },
        colors = CardDefaults.cardColors(containerColor = colorTarjeta)
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

@Composable
private fun SolicitudReposicionDialog(
    borradorInicial: BorradorCorreo,
    validarDestinatario: (String) -> String?,
    onCancelar: () -> Unit,
    onConfirmar: (BorradorCorreo) -> Unit
) {
    var destinatario by remember { mutableStateOf(borradorInicial.destinatario) }
    var asunto by remember { mutableStateOf(borradorInicial.asunto) }
    var cuerpo by remember { mutableStateOf(borradorInicial.cuerpo) }
    var errorDestinatario by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { Icon(Icons.Default.Email, contentDescription = null) },
        title = { Text("Solicitud de reposición") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Revisa y modifica el correo. Se abrirá tu app de correo con un borrador; " +
                            "el envío lo haces tú.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = destinatario,
                    onValueChange = {
                        destinatario = it
                        errorDestinatario = null
                    },
                    label = { Text("Para (correo ficticio)") },
                    isError = errorDestinatario != null,
                    supportingText = { Text(errorDestinatario ?: "") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = asunto,
                    onValueChange = { asunto = it },
                    label = { Text("Asunto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cuerpo,
                    onValueChange = { cuerpo = it },
                    label = { Text("Mensaje (productos y cantidades)") },
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val error = validarDestinatario(destinatario)
                if (error != null) {
                    errorDestinatario = error
                } else {
                    onConfirmar(BorradorCorreo(destinatario.trim(), asunto, cuerpo))
                }
            }) { Text("Abrir correo") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
