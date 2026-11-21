package com.example.dsy1105_006d_lc7.data.remote

import retrofit2.http.GET

interface ProveedorApi {
    @GET("proveedores.json")
    suspend fun obtenerProveedores(): List<ProveedorDto>
}