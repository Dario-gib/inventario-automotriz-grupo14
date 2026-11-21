package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ReposicionAgrupadorTest : StringSpec({

    fun producto(id: Int, codigo: String, categoria: String, cantidad: Int = 2, minimo: Int = 5) =
        ProductoEntity(
            id = id, codigo = codigo, nombre = "Producto $codigo",
            categoria = categoria, cantidad = cantidad, stockMinimo = minimo
        )

    fun proveedor(id: Int, nombre: String, categorias: String, estado: String = "Activo") =
        ProveedorEntity(
            id = id, codigo = "PRV-00$id", nombre = nombre,
            correo = "prov$id@example.com", categorias = categorias, estado = estado
        )

    val andes = proveedor(1, "Repuestos Andes", "Motor,Encendido,Frenos")
    val sur = proveedor(2, "Baterías Sur", "Baterías,Neumáticos")

    "agrupa los productos con stock bajo por proveedor según la categoría" {
        val productos = listOf(
            producto(1, "BUJ-001", "Encendido"),
            producto(2, "BAT-001", "Baterías"),
            producto(3, "MOT-001", "Motor")
        )

        val plan = agruparPorProveedor(productos, listOf(andes, sur))

        plan.grupos.map { it.proveedor.nombre } shouldBe listOf("Baterías Sur", "Repuestos Andes")
        plan.grupos.first { it.proveedor.id == 1 }.productos.map { it.codigo } shouldBe
                listOf("BUJ-001", "MOT-001")
        plan.sinProveedor shouldBe emptyList()
    }

    "ignora los productos que no tienen stock bajo" {
        val productos = listOf(producto(1, "BUJ-001", "Encendido", cantidad = 20))

        val plan = agruparPorProveedor(productos, listOf(andes, sur))

        plan.grupos shouldBe emptyList()
        plan.sinProveedor shouldBe emptyList()
    }

    "un proveedor inactivo no recibe productos" {
        val inactivo = andes.copy(estado = "Inactivo")
        val productos = listOf(producto(1, "BUJ-001", "Encendido"))

        val plan = agruparPorProveedor(productos, listOf(inactivo, sur))

        plan.grupos shouldBe emptyList()
        plan.sinProveedor.map { it.codigo } shouldBe listOf("BUJ-001")
    }

    "compara las categorías sin importar tildes ni mayúsculas" {
        val productos = listOf(producto(1, "BAT-001", "baterias"))

        val plan = agruparPorProveedor(productos, listOf(andes, sur))

        plan.grupos.map { it.proveedor.nombre } shouldBe listOf("Baterías Sur")
    }

    "un producto sin proveedor que atienda su categoría queda aparte" {
        val productos = listOf(producto(1, "CAR-001", "Carrocería"))

        val plan = agruparPorProveedor(productos, listOf(andes, sur))

        plan.grupos shouldBe emptyList()
        plan.sinProveedor.map { it.codigo } shouldBe listOf("CAR-001")
    }
})