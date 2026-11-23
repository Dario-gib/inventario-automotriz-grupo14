package com.example.dsy1105_006d_lc7.ui.producto

import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.UbicacionEntity
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest : StringSpec({

    val dispatcher = UnconfinedTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    fun producto(id: Int = 0, codigo: String = "FRE-001", cantidad: Int = 10, minimo: Int = 5) =
        ProductoEntity(
            id = id, codigo = codigo, nombre = "Pastillas de freno",
            categoria = "Frenos", cantidad = cantidad, stockMinimo = minimo
        )

    // Repositorio simulado: por defecto no hay ningún producto con ese código
    fun repoSimulado(productos: List<ProductoEntity> = emptyList()): ProductoRepository {
        val repo = mockk<ProductoRepository>()
        every { repo.observarTodos() } returns flowOf(productos)
        every { repo.buscar(any()) } returns flowOf(productos)
        coEvery { repo.obtenerPorCodigo(any()) } returns null
        coEvery { repo.insertar(any()) } returns Unit
        coEvery { repo.actualizar(any()) } returns Unit
        coEvery { repo.eliminar(any()) } returns Unit
        return repo
    }

    fun llenarFormularioValido(vm: ProductoViewModel) {
        vm.onCodigoChange("FRE-001")
        vm.onNombreChange("Pastillas de freno")
        vm.onCategoriaChange("Frenos")
        vm.onCantidadChange("10")
        vm.onStockMinimoChange("5")
    }

    // ---------- Validaciones ----------

    "guardar con el formulario vacío muestra los errores y no guarda" {
        runTest(dispatcher) {
            val repo = repoSimulado()
            val vm = ProductoViewModel(repo)

            vm.guardar()

            val s = vm.form.value
            s.codigoError shouldBe "El código es obligatorio"
            s.nombreError shouldBe "El nombre es obligatorio"
            s.categoriaError shouldBe "La categoría es obligatoria"
            s.cantidadError shouldBe "Campo obligatorio"
            s.stockMinimoError shouldBe "Campo obligatorio"
            coVerify(exactly = 0) { repo.insertar(any()) }
        }
    }

    "una cantidad negativa muestra error" {
        runTest(dispatcher) {
            val vm = ProductoViewModel(repoSimulado())
            llenarFormularioValido(vm)
            vm.onCantidadChange("-1")

            vm.guardar()

            vm.form.value.cantidadError shouldBe "Debe ser mayor o igual a 0"
        }
    }

    "una cantidad con texto muestra error" {
        runTest(dispatcher) {
            val vm = ProductoViewModel(repoSimulado())
            llenarFormularioValido(vm)
            vm.onCantidadChange("abc")

            vm.guardar()

            vm.form.value.cantidadError shouldBe "Debe ser un número entero"
        }
    }

    "escribir en un campo limpia su error" {
        runTest(dispatcher) {
            val vm = ProductoViewModel(repoSimulado())
            vm.guardar()
            vm.form.value.nombreError shouldBe "El nombre es obligatorio"

            vm.onNombreChange("Filtro")

            vm.form.value.nombreError shouldBe null
        }
    }

    // ---------- Guardar, editar y eliminar ----------

    "guardar un producto válido lo inserta y avisa que se guardó" {
        runTest(dispatcher) {
            val repo = repoSimulado()
            val vm = ProductoViewModel(repo)
            llenarFormularioValido(vm)

            vm.guardar()

            coVerify(exactly = 1) {
                repo.insertar(match { it.codigo == "FRE-001" && it.cantidad == 10 && it.stockMinimo == 5 })
            }
            vm.form.value.guardadoExitosamente shouldBe true
            vm.mensaje.value shouldBe "Producto creado"
        }
    }

    "un código repetido muestra error y no inserta" {
        runTest(dispatcher) {
            val repo = repoSimulado()
            coEvery { repo.obtenerPorCodigo("FRE-001") } returns producto(id = 7)
            val vm = ProductoViewModel(repo)
            llenarFormularioValido(vm)

            vm.guardar()

            vm.form.value.codigoError shouldBe "Ya existe un producto con este código"
            vm.form.value.guardando shouldBe false
            coVerify(exactly = 0) { repo.insertar(any()) }
        }
    }

    "editar un producto existente llama a actualizar" {
        runTest(dispatcher) {
            val existente = producto(id = 5)
            val repo = repoSimulado()
            coEvery { repo.obtenerPorId(5) } returns existente
            coEvery { repo.obtenerPorCodigo("FRE-001") } returns existente
            val vm = ProductoViewModel(repo)

            vm.cargarProducto(5)
            vm.form.value.esEdicion shouldBe true
            vm.onNombreChange("Pastillas de freno delanteras")
            vm.guardar()

            coVerify(exactly = 1) {
                repo.actualizar(match { it.id == 5 && it.nombre == "Pastillas de freno delanteras" })
            }
            coVerify(exactly = 0) { repo.insertar(any()) }
            vm.mensaje.value shouldBe "Producto actualizado"
        }
    }

    "eliminar un producto lo elimina del repositorio" {
        runTest(dispatcher) {
            val repo = repoSimulado()
            val vm = ProductoViewModel(repo)
            val p = producto(id = 3)

            vm.eliminar(p)

            coVerify(exactly = 1) { repo.eliminar(p) }
        }
    }

    // ---------- Lista y resumen ----------

    "la lista calcula el total y los productos con stock bajo" {
        runTest(dispatcher) {
            val repo = repoSimulado(
                listOf(
                    producto(1, "A-1", cantidad = 2),
                    producto(2, "A-2", cantidad = 20),
                    producto(3, "A-3", cantidad = 5)
                )
            )
            val vm = ProductoViewModel(repo)
            backgroundScope.launch(dispatcher) { vm.lista.collect { } }

            vm.lista.value.total shouldBe 3
            vm.lista.value.stockBajo shouldBe 2
        }
    }

    "el filtro de stock bajo deja solo esos productos" {
        runTest(dispatcher) {
            val repo = repoSimulado(
                listOf(
                    producto(1, "A-1", cantidad = 2),
                    producto(2, "A-2", cantidad = 20),
                    producto(3, "A-3", cantidad = 5)
                )
            )
            val vm = ProductoViewModel(repo)
            backgroundScope.launch(dispatcher) { vm.lista.collect { } }

            vm.onSoloStockBajoChange(true)

            vm.lista.value.productos.map { it.codigo } shouldBe listOf("A-1", "A-3")
        }
    }

    // ---------- Reposición por correo ----------

    "validarDestinatario revisa el correo" {
        val vm = ProductoViewModel(repoSimulado())

        vm.validarDestinatario("") shouldBe "El destinatario es obligatorio"
        vm.validarDestinatario("correo-malo") shouldBe "Ingresa un correo válido"
        vm.validarDestinatario("proveedor@example.com") shouldBe null
    }

    "el borrador incluye el producto y la cantidad sugerida" {
        val vm = ProductoViewModel(repoSimulado())

        val borrador = vm.generarBorrador(listOf(producto(cantidad = 2, minimo = 5)))

        borrador.asunto shouldBe "Solicitud de cotización de reposición"
        borrador.cuerpo shouldContain "Pastillas de freno"
        borrador.cuerpo shouldContain "solicitar 8 unidades"
    }

    "la marca se guarda junto con el producto" {
        runTest(dispatcher) {
            val repo = repoSimulado()
            val vm = ProductoViewModel(repo)
            llenarFormularioValido(vm)
            vm.onMarcaChange("  Bosch ")

            vm.guardar()

            coVerify(exactly = 1) { repo.insertar(match { it.marca == "Bosch" }) }
        }
    }

    "la marca es opcional" {
        runTest(dispatcher) {
            val repo = repoSimulado()
            val vm = ProductoViewModel(repo)
            llenarFormularioValido(vm)

            vm.guardar()

            coVerify(exactly = 1) { repo.insertar(match { it.marca == "" }) }
            vm.form.value.guardadoExitosamente shouldBe true
        }
    }

    "al editar se carga la marca del producto" {
        runTest(dispatcher) {
            val repo = repoSimulado()
            coEvery { repo.obtenerPorId(5) } returns producto(id = 5).copy(marca = "NGK")
            val vm = ProductoViewModel(repo)

            vm.cargarProducto(5)

            vm.form.value.marca shouldBe "NGK"
        }
    }

    "las ubicaciones se indexan por el id del producto" {
        runTest(dispatcher) {
            val ub1 = UbicacionEntity(productoId = 1, zona = "A", estante = "3", repisa = "2")
            val ub2 = UbicacionEntity(productoId = 2, zona = "B", estante = "1", repisa = "4")
            val vm = ProductoViewModel(repoSimulado(), flowOf(listOf(ub1, ub2)))
            backgroundScope.launch(dispatcher) { vm.ubicaciones.collect { } }

            vm.ubicaciones.value[1] shouldBe ub1
            vm.ubicaciones.value[2] shouldBe ub2
        }
    }

    "un producto sin ubicación no aparece en el mapa" {
        runTest(dispatcher) {
            val vm = ProductoViewModel(repoSimulado(), flowOf(emptyList()))
            backgroundScope.launch(dispatcher) { vm.ubicaciones.collect { } }

            vm.ubicaciones.value[99] shouldBe null
        }
    }

})