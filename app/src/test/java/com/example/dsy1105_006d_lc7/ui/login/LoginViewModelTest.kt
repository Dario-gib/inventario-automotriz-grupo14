package com.example.dsy1105_006d_lc7.ui.login

import com.example.dsy1105_006d_lc7.data.repository.AuthRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class LoginViewModelTest : StringSpec({

    "con credenciales correctas se ejecuta onSuccess con el usuario" {
        val repo = mockk<AuthRepository>()
        every { repo.login("admin", "123") } returns true
        val vm = LoginViewModel(repo)

        vm.onUsernameChange("admin")
        vm.onPasswordChange("123")
        var recibido: String? = null
        vm.submit { recibido = it }

        recibido shouldBe "admin"
        vm.uiState.value.error shouldBe null
        verify(exactly = 1) { repo.login("admin", "123") }
    }

    "con credenciales incorrectas muestra error y no ejecuta onSuccess" {
        val repo = mockk<AuthRepository>()
        every { repo.login(any(), any()) } returns false
        val vm = LoginViewModel(repo)

        vm.onUsernameChange("admin")
        vm.onPasswordChange("999")
        var llamado = false
        vm.submit { llamado = true }

        llamado shouldBe false
        vm.uiState.value.error shouldBe "Credenciales inválidas"
    }

    "los campos vacíos muestran error" {
        val vm = LoginViewModel(AuthRepository())
        var llamado = false
        vm.submit { llamado = true }

        llamado shouldBe false
        vm.uiState.value.error shouldBe "Credenciales inválidas"
    }

    "el usuario se recorta antes de validar" {
        val vm = LoginViewModel(AuthRepository())
        vm.onUsernameChange("  admin  ")
        vm.onPasswordChange("123")
        var recibido: String? = null
        vm.submit { recibido = it }

        recibido shouldBe "admin"
    }

    "escribir en un campo limpia el error" {
        val vm = LoginViewModel(AuthRepository())
        vm.submit { }
        vm.uiState.value.error shouldBe "Credenciales inválidas"

        vm.onUsernameChange("a")

        vm.uiState.value.error shouldBe null
    }
})