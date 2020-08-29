package com.firebase.firestoreapp.adapter

import com.firebase.firestoreapp.model.Crypto

interface CryptosAdapterListener {
    fun onBuyCryptoClick(crypto:Crypto)
}