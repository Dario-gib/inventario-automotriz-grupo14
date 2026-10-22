package com.example.dsy1105_006d_lc7.ui.producto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.stockBajo
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductoRepository(
        AppDatabase.getDatabase(application).productoDao()
    )

    // ---------- Formulario ----------
    private val _form = MutableStateFlow(ProductoUiState())
    val form: StateFlow<ProductoUiState> = _form.asStateFlow()

    // ---------- Mensajes para la lista (guardado, edición) ----------
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    // ---------- Lista (reactiva con búsqueda y filtros) ----------
    private val _busqueda = MutableStateFlow("")
    private val _soloStockBajo = MutableStateFlow(false)
    private val _categoria = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val lista: StateFlow<ListaProductosUiState> = combine(
        repository.observarTodos(),
        _busqueda.flatMapLatest { texto ->
            if (texto.isBlank()) repository.observarTodos()
            else repository.buscar(texto.trim())
        },
        _busqueda,
        _soloStockBajo,
        _categoria
    ) { todos, resultado, texto, soloBajo, categoria ->
        val categorias = todos
            .map { it.categoria.trim() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
        // Si la categoría elegida ya no existe (por ejemplo, se eliminó), se ignora
        val categoriaActiva = categoria?.takeIf { sel ->
            categorias.any { it.equals(sel, ignoreCase = true) }
        }

        var visibles = resultado
        if (soloBajo) visibles = visibles.filter { it.stockBajo }
        if (categoriaActiva != null) {
            visibles = visibles.filter { it.categoria.equals(categoriaActiva, ignoreCase = true) }
        }

        ListaProductosUiState(
            productos = visibles,
            busqueda = texto,
            soloStockBajo = soloBajo,
            total = todos.size,
            stockBajo = todos.count { it.stockBajo },
            categorias = categorias,
            categoriaSeleccionada = categoriaActiva
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListaProductosUiState())

    fun onBusquedaChange(texto: String) {
        _busqueda.value = texto
    }

    fun onSoloStockBajoChange(valor: Boolean) {
        _soloStockBajo.value = valor
    }

    fun onCategoriaSeleccionada(categoria: String?) {
        _categoria.value = categoria
    }

    // ---------- Cambios de campos (limpian su error) ----------
    fun onCodigoChange(v: String) = _form.update { it.copy(codigo = v, codigoError = null) }
    fun onNombreChange(v: String) = _form.update { it.copy(nombre = v, nombreError = null) }
    fun onCategoriaChange(v: String) = _form.update { it.copy(categoria = v, categoriaError = null) }
    fun onCantidadChange(v: String) = _form.update { it.copy(cantidad = v, cantidadError = null) }
    fun onStockMinimoChange(v: String) = _form.update { it.copy(stockMinimo = v, stockMinimoError = null) }

    // ---------- Preparar el formulario ----------
    fun nuevoProducto() {
        _form.value = ProductoUiState()
    }

    fun cargarProducto(id: Int) {
        viewModelScope.launch {
            val p = repository.obtenerPorId(id) ?: return@launch
            _form.value = ProductoUiState(
                id = p.id,
                codigo = p.codigo,
                nombre = p.nombre,
                categoria = p.categoria,
                cantidad = p.cantidad.toString(),
                stockMinimo = p.stockMinimo.toString()
            )
        }
    }

    // ---------- Validaciones ----------
    private fun validarNumero(valor: String): String? {
        if (valor.isBlank()) return "Campo obligatorio"
        val numero = valor.trim().toIntOrNull() ?: return "Debe ser un número entero"
        if (numero < 0) return "Debe ser mayor o igual a 0"
        return null
    }

    private fun validar(): Boolean {
        val s = _form.value
        val codigoError = if (s.codigo.isBlank()) "El código es obligatorio" else null
        val nombreError = if (s.nombre.isBlank()) "El nombre es obligatorio" else null
        val categoriaError = if (s.categoria.isBlank()) "La categoría es obligatoria" else null
        val cantidadError = validarNumero(s.cantidad)
        val stockMinimoError = validarNumero(s.stockMinimo)

        _form.update {
            it.copy(
                codigoError = codigoError,
                nombreError = nombreError,
                categoriaError = categoriaError,
                cantidadError = cantidadError,
                stockMinimoError = stockMinimoError
            )
        }
        return listOf(
            codigoError, nombreError, categoriaError, cantidadError, stockMinimoError
        ).all { it == null }
    }

    // ---------- Guardar (crear o modificar) ----------
    fun guardar() {
        if (!validar()) return
        val s = _form.value

        viewModelScope.launch {
            _form.update { it.copy(guardando = true) }

            // El código debe ser único
            val existente = repository.obtenerPorCodigo(s.codigo.trim())
            if (existente != null && existente.id != s.id) {
                _form.update {
                    it.copy(guardando = false, codigoError = "Ya existe un producto con este código")
                }
                return@launch
            }

            val entidad = ProductoEntity(
                id = s.id,
                codigo = s.codigo.trim(),
                nombre = s.nombre.trim(),
                categoria = s.categoria.trim(),
                cantidad = s.cantidad.trim().toInt(),
                stockMinimo = s.stockMinimo.trim().toInt()
            )
            if (s.esEdicion) repository.actualizar(entidad) else repository.insertar(entidad)

            _mensaje.value = if (s.esEdicion) "Producto actualizado" else "Producto creado"
            _form.update { it.copy(guardando = false, guardadoExitosamente = true) }
        }
    }

    // Se llama después de navegar, para que no se repita la navegación
    fun consumirGuardado() {
        _form.update { it.copy(guardadoExitosamente = false) }
    }

    fun eliminar(producto: ProductoEntity) {
        viewModelScope.launch { repository.eliminar(producto) }
    }

    // Deshacer una eliminación: vuelve a insertar el mismo producto
    fun restaurar(producto: ProductoEntity) {
        viewModelScope.launch { repository.insertar(producto) }
    }
}