package com.example.dsy1105_006d_lc7.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun ProductoFormScreen(
    navController: NavController,
    nombre:String,
    precio: String
){// Inicio Formulario

    val cantidad by remember { mutableStateOf(TextFieldValue("")) }


}// termino Inicio Formulario


@Preview(showBackground = true)
@Composable

fun PreviewProductoFormScreen(){
    ProductoFormScreen(
        navController = rememberNavController(),
        nombre="Producto Ejemplo",
        precio="$10.000"
    )
}