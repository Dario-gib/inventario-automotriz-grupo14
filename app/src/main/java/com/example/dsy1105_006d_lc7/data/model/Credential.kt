package com.example.dsy1105_006d_lc7.data.model

data class Credential (val username:String, val password:String){
    companion object{
        val Admin = Credential(username="admin", password="123")
    }

}