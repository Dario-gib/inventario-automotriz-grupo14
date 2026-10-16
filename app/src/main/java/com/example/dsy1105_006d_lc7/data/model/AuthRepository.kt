package com.example.dsy1105_006d_lc7.data.model

class AuthRepository (
    private val validCredential: Credential = Credential.Admin
){
    fun login(username:String,password:String): Boolean{
        return username== validCredential.username && password ==validCredential.password
    }


}