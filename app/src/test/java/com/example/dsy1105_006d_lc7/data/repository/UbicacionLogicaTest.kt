package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.UbicacionEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class UbicacionLogicaTest : StringSpec({

    fun producto(id: Int, codigo: String, nombre: String) = ProductoEntity(
        id = id, codigo = codigo, nombre = nombre, categoria = "Motor",
        cantidad = 10, stockMinimo = 5
    )

    val bujias = producto(1, "BUJ-001", "Bujías")
    val bateria = producto(2, "BAT-001", "Batería")
    val ubBujias = UbicacionEntity(productoId = 1, zona = "A", estante = "3", repisa = "2")
    val lista = combinarProductosConUbicacion(listOf(bujias, bateria), listOf(ubBujias))

    "cada producto recibe su ubicación" {
        lista[0].ubicacion shouldBe ubBujias
        lista[1].ubicacion shouldBe null
    }

    "el texto de la ubicación muestra zona, estante y repisa" {
        ubBujias.texto shouldBe "Zona A · Estante 3 · Repisa 2"
    }

    "un producto sin ubicación lo indica" {
        lista[1].textoUbicacion() shouldBe "Sin ubicación asignada"
    }

    "filtra por nombre sin importar tildes" {
        filtrarPorTexto(lista, "bujias").map { it.producto.codigo } shouldBe listOf("BUJ-001")
    }

    "filtra por código" {
        filtrarPorTexto(lista, "bat-001").map { it.producto.codigo } shouldBe listOf("BAT-001")
    }

    "filtra por ubicación" {
        filtrarPorTexto(lista, "estante 3").map { it.producto.codigo } shouldBe listOf("BUJ-001")
    }

    "filtra los productos sin ubicación" {
        filtrarPorTexto(lista, "sin ubicacion").map { it.producto.codigo } shouldBe listOf("BAT-001")
    }

    "un texto vacío no filtra nada" {
        filtrarPorTexto(lista, "  ").size shouldBe 2
    }
})