package com.example.dsy1105_006d_lc7.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://raw.githubusercontent.com/Dario-gib/puenteauto-api/main/"

    val proveedorApi: ProveedorApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProveedorApi::class.java)
    }
}