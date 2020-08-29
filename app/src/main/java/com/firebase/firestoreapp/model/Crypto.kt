package com.firebase.firestoreapp.model

class Crypto( var name:String = "",var imageUrl:String = "",var available:Int = 0) {
    fun getDocuments():String{
        return name.toLowerCase()
    }

    override fun toString(): String {
        return "Crypto(name='$name', imageURL='$imageUrl', available=$available)"
    }


}