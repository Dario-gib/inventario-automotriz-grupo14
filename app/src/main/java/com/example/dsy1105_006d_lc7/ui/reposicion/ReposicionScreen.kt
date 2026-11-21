package com.example.dsy1105_006d_lc7.ui.reposicion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.cantidadSugerida
import com.example.dsy1105_006d_lc7.data.repository.GrupoReposicion
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors
import com.example.dsy1105_006d_lc7.utils.CorreoHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposicionScreen(
    viewModel: ReposicionViewModel,
    onVerProveedores: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reposición") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onVerProveedores) {
                        Icon(Icons.Default.LocalShipping, contentDescription = "Ver proveedores")
                    }
                    IconButton(onClick = viewModel::sincronizar) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar proveedores desde la API")
                    }
                },
                colors = appTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val plan = estado.plan
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (estado.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (plan.grupos.isEmpty() && plan.sinProveedor.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay productos con stock bajo.\nPor ahora no se necesita reposición.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(plan.grupos, key = { it.proveedor.id }) { grupo ->
                        GrupoCard(
                            grupo = grupo,
                            onPreparar = {
                                val b = viewModel.generarBorrador(grupo)
                                val abierto = CorreoHelper.abrirBorrador(
                                    context, b.destinatario, b.asunto, b.cuerpo
                                )
                                if (!abierto) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("No hay una aplicación de correo instalada")
                                    }
                                }
                            }
                        )
                    }
                    if (plan.sinProveedor.isNotEmpty()) {
                        item { SinProveedorCard(plan.sinProveedor) }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrupoCard(grupo: GrupoReposicion, onPreparar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(grupo.proveedor.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${grupo.proveedor.correo} · ${grupo.productos.size} producto(s)")
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            grupo.productos.forEach { p ->
                Text("• ${p.nombre} (${p.codigo}): disponible ${p.cantidad}, solicitar ${p.cantidadSugerida}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onPreparar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Email, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Preparar correo")
            }
        }
    }
}

@Composable
private fun SinProveedorCard(productos: List<ProductoEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Sin proveedor asignado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Ningún proveedor activo atiende la categoría de estos productos:")
            productos.forEach { p ->
                Text("• ${p.nombre} (${p.codigo}) · categoría ${p.categoria}")
            }
        }
    }
}