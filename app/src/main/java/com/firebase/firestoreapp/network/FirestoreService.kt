package com.firebase.firestoreapp.network

import android.util.Log
import com.firebase.firestoreapp.model.Crypto
import com.firebase.firestoreapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

const val CRYPTO_COLLECTION_NAME  = "cryptos"
const val USERS_COLLECTION_NAME  = "users"
class FirestoreService(val firebaseFirestore:FirebaseFirestore) {

    fun setDocument(data:Any,collectionName:String,id:String,callback: Callback<Void>){
            firebaseFirestore.collection(collectionName).document(id).set(data)
                .addOnSuccessListener { callback.onSuccess(null) }
                .addOnFailureListener {exception: Exception ->  callback.onFailed(exception)}
    }

    fun updateUser(user:User,callback: Callback<User>?){
        firebaseFirestore.collection(USERS_COLLECTION_NAME).document(user.username)
            .update("cryptoList",user.cryptoList)
            .addOnSuccessListener {
                callback?.onSuccess(user)
            }
            .addOnFailureListener{ exception: Exception -> callback?.onFailed(exception) }
    }
    fun updateCrypto(crypto: Crypto){
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).document(crypto.getDocuments())
            .update("available",crypto.available)
    }

    fun getCryptos(callback: Callback<List<Crypto>>?){
    firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).get()
        .addOnSuccessListener {list ->
            for (document in list){
                val cryptoList = list.toObjects(Crypto::class.java)
                callback?.onSuccess(cryptoList)
                break
            }
        }
        .addOnFailureListener {exception ->
            callback?.onFailed(exception)
        }
    }

    fun findUserByID(id:String,callback: Callback<User>?){
        firebaseFirestore.collection(USERS_COLLECTION_NAME).document(id).get()
            .addOnSuccessListener { user ->
                    if(user.data != null){
                        callback?.onSuccess(user.toObject(User::class.java))
                    }else{
                        callback?.onSuccess(null)
                    }
            }
            .addOnFailureListener { exception -> callback?.onFailed(exception)}
    }

    fun listenForUpdates(cryptos:List<Crypto>,listener: RealtimeDataListener<Crypto>){
        val cryptoReference = firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
        for(crypto in cryptos){
            cryptoReference.document(crypto.getDocuments()).addSnapshotListener { snapshot ,error ->
                    if(error !=  null){
                        listener.onError(error)
                    }
                if(snapshot != null && snapshot.exists()){
                    listener.onDataChange(snapshot.toObject(Crypto::class.java)!!)
                }
            }
        }
    }

    fun listenForUpdates(user:User,listener: RealtimeDataListener<User>){
        val usersReference = firebaseFirestore.collection(USERS_COLLECTION_NAME)
        usersReference.document(user.username).addSnapshotListener { snapshot,error ->
            if(error !=  null){
                listener.onError(error)
            }
            if(snapshot != null && snapshot.exists()){
                listener.onDataChange(snapshot.toObject(User::class.java)!!)
            }
        }
    }


}