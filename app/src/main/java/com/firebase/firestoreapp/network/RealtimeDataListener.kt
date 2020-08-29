package com.firebase.firestoreapp.network

import java.lang.Exception

interface RealtimeDataListener<T> {
    fun onDataChange(updatedData:T)
    fun onError(exception: Exception)
}