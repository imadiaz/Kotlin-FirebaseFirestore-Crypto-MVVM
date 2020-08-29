package com.firebase.firestoreapp.model

class User (user:String = ""){
    val username:String=user
    var cryptoList:List<Crypto>?=null
    override fun toString(): String {
        return "User(username='$username', cryptoList=$cryptoList)"
    }


}