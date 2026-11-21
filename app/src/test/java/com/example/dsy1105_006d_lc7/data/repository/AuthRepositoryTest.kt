package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.model.Credential
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AuthRepositoryTest : StringSpec({

    "acepta las credenciales por defecto" {
        AuthRepository().login("admin", "123") shouldBe true
    }

    "rechaza una contraseña incorrecta" {
        AuthRepository().login("admin", "999") shouldBe false
    }

    "rechaza un usuario incorrecto" {
        AuthRepository().login("otro", "123") shouldBe false
    }

    "usa la credencial que se le entrega" {
        val repo = AuthRepository(Credential("taller", "pin"))
        repo.login("taller", "pin") shouldBe true
        repo.login("admin", "123") shouldBe false
    }
})