package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.UbicacionDao
import com.example.dsy1105_006d_lc7.data.local.UbicacionEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class UbicacionRepositoryTest : StringSpec({

    val ubicacion = UbicacionEntity(productoId = 1, zona = "A", estante = "3", repisa = "2")

    fun daoSimulado(): UbicacionDao {
        val dao = mockk<UbicacionDao>()
        every { dao.observarTodas() } returns flowOf(listOf(ubicacion))
        coEvery { dao.guardar(any()) } returns Unit
        coEvery { dao.obtener(1) } returns ubicacion
        return dao
    }

    "guardar entrega la ubicación al DAO" {
        val dao = daoSimulado()
        val repo = UbicacionRepository(dao)

        repo.guardar(ubicacion)

        coVerify(exactly = 1) { dao.guardar(ubicacion) }
    }

    "obtener devuelve la ubicación del producto" {
        val repo = UbicacionRepository(daoSimulado())

        repo.obtener(1) shouldBe ubicacion
    }
})