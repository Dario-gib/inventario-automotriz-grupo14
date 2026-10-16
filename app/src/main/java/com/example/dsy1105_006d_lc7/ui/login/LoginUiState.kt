package com.example.dsy1105_006d_lc7.ui.login

data class LoginUiState (
    val username:String="",
    val password:String="",
    val isLoading:Boolean =false,
    val error:String? =null
)