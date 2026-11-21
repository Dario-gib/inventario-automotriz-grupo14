package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.ProveedorDao
import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import com.example.dsy1105_006d_lc7.data.remote.ProveedorApi
import com.example.dsy1105_006d_lc7.data.remote.ProveedorDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import java.io.IOException

class ProveedorRepositoryTest : StringSpec({

    val andes = ProveedorDto(
        codigo = "PRV-001", nombre = "Repuestos Andes", correo = "ventas@andes.example",
        telefono = "+56 9 0000 0001", categorias = listOf("Motor", "Encendido"), estado = "Activo"
    )
    val sur = ProveedorDto(
        codigo = "PRV-002", nombre = "Baterías Sur", correo = "pedidos@sur.example",
        telefono = null, categorias = listOf("Baterías"), estado = "Activo"
    )

    fun daoSimulado(): ProveedorDao {
        val dao = mockk<ProveedorDao>()
        every { dao.observarTodos() } returns flowOf(emptyList())
        coEvery { dao.reemplazarTodos(any()) } returns Unit
        return dao
    }

    "sincronizar guarda en Room los proveedores que entrega la API" {
        val api = mockk<ProveedorApi>()
        val dao = daoSimulado()
        coEvery { api.obtenerProveedores() } returns listOf(andes, sur)
        val repo = ProveedorRepository(api, dao)

        repo.sincronizar() shouldBe true

        coVerify(exactly = 1) {
            dao.reemplazarTodos(match {
                it.size == 2 && it[0].codigo == "PRV-001" && it[0].categorias == "Motor,Encendido"
            })
        }
    }

    "si la API falla devuelve false y no toca los datos locales" {
        val api = mockk<ProveedorApi>()
        val dao = daoSimulado()
        coEvery { api.obtenerProveedores() } throws IOException("Sin conexión")
        val repo = ProveedorRepository(api, dao)

        repo.sincronizar() shouldBe false

        coVerify(exactly = 0) { dao.reemplazarTodos(any()) }
    }

    "un proveedor sin teléfono ni estado se guarda con valores por defecto" {
        val dto = ProveedorDto("PRV-009", "Sin datos", "x@x.example", null, null, null)
        // compilo bien pero marca null de categoria y de estado

        val entidad = dto.toEntity()

        entidad.telefono shouldBe ""
        entidad.categorias shouldBe ""
        entidad.estado shouldBe "Activo"
    }

    "las categorías guardadas se convierten en lista" {
        val entidad = ProveedorEntity(
            codigo = "PRV-001", nombre = "Andes", correo = "a@a.example",
            categorias = "Motor, Encendido,,Frenos", estado = "Activo"
        )

        entidad.listaCategorias shouldBe listOf("Motor", "Encendido", "Frenos")
    }

    "solo el estado Activo se considera activo" {
        val base = ProveedorEntity(
            codigo = "P", nombre = "N", correo = "c@c.example", categorias = "", estado = "Activo"
        )

        base.activo shouldBe true
        base.copy(estado = "Inactivo").activo shouldBe false
    }
})