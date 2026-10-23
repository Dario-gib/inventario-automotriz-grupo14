package com.example.dsy1105_006d_lc7.ui.producto

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormScreen(
    viewModel: ProductoViewModel,
    onEscanearCodigo: () -> Unit,
    onVolver: () -> Unit
) {
    val state by viewModel.form.collectAsState()

    // Cuando se guarda bien, vuelve a la lista
    LaunchedEffect(state.guardadoExitosamente) {
        if (state.guardadoExitosamente) {
            viewModel.consumirGuardado()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.esEdicion) "Editar producto" else "Nuevo producto") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = appTopBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampoTexto(
                label = "Código",
                valor = state.codigo,
                onChange = viewModel::onCodigoChange,
                error = state.codigoError,
                iconoInicial = Icons.Default.Inventory2,
                trailingIcon = {
                    IconButton(onClick = onEscanearCodigo) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear código QR"
                        )
                    }
                }
            )
            CampoTexto(
                "Nombre", state.nombre, viewModel::onNombreChange, state.nombreError,
                iconoInicial = Icons.Default.Edit
            )
            CampoTexto(
                "Categoría", state.categoria, viewModel::onCategoriaChange, state.categoriaError,
                iconoInicial = Icons.Default.Category
            )
            CampoTexto(
                "Cantidad", state.cantidad, viewModel::onCantidadChange, state.cantidadError,
                iconoInicial = Icons.Default.ShoppingCart,
                tipoTeclado = KeyboardType.Number
            )
            CampoTexto(
                "Stock mínimo", state.stockMinimo, viewModel::onStockMinimoChange,
                state.stockMinimoError,
                iconoInicial = Icons.Default.Warning,
                tipoTeclado = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::guardar,
                enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.guardando) "Guardando..." else "Guardar")
            }
        }
    }
}

@Composable
private fun CampoTexto(
    label: String,
    valor: String,
    onChange: (String) -> Unit,
    error: String?,
    iconoInicial: ImageVector,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    // Si el campo tiene un error y no trae un botón propio, muestra un ícono de error
    val iconoError: @Composable () -> Unit = {
        Icon(
            Icons.Default.Error,
            contentDescription = "Campo con error",
            tint = MaterialTheme.colorScheme.error
        )
    }
    val iconoFinal: (@Composable () -> Unit)? =
        trailingIcon ?: if (error != null) iconoError else null

    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(iconoInicial, contentDescription = null) },
        isError = error != null,
        // El mensaje aparece y desaparece con un fundido suave
        supportingText = {
            Crossfade(targetState = error, label = "errorCampo") { mensaje ->
                Text(mensaje ?: "")
            }
        },
        singleLine = true,
        trailingIcon = iconoFinal,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        modifier = Modifier.fillMaxWidth()
    )
}
