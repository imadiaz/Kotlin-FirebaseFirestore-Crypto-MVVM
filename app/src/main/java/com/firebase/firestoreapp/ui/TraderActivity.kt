package com.firebase.firestoreapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.firestoreapp.R
import com.firebase.firestoreapp.adapter.CryptosAdapter
import com.firebase.firestoreapp.adapter.CryptosAdapterListener
import com.firebase.firestoreapp.model.Crypto
import com.firebase.firestoreapp.model.User
import com.firebase.firestoreapp.network.Callback
import com.firebase.firestoreapp.network.FirestoreService
import com.firebase.firestoreapp.network.RealtimeDataListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_trader.*
import java.lang.Exception

class TraderActivity : AppCompatActivity(), CryptosAdapterListener {

    private lateinit var firestoreService: FirestoreService
    private val cryptoAdapter:CryptosAdapter = CryptosAdapter(this)
    private lateinit var username:String
    private lateinit var user:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trader)
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())


        username = intent.extras!![USERNAME_KEY]!!.toString()
            usernameTextView.text = username
        configureRecyclerView()
        loadCryptos()
        fab.setOnClickListener { view ->
                generateCryptoCurrencyRandom()
        }
    }

    private fun generateCryptoCurrencyRandom() {
        for(crypto in cryptoAdapter.cryptoList){
            val amount = (1..50).random()
            crypto.available+=amount
            firestoreService.updateCrypto(crypto)
        }
    }

    private fun loadCryptos() {
        firestoreService.getCryptos(object:Callback<List<Crypto>>{
            override fun onSuccess(cryptoList: List<Crypto>?) {
                firestoreService.findUserByID(username,object:Callback<User>{
                    override fun onSuccess(result: User?) {
                        user = result!!
                        if(user.cryptoList == null){
                            val userCryptoList = mutableListOf<Crypto>()
                            for(crypto in cryptoList!!){
                                    val cryptoUser = Crypto()
                                    cryptoUser.name = crypto.name
                                    cryptoUser.available = crypto.available
                                    cryptoUser.imageUrl = crypto.imageUrl
                                    userCryptoList.add(crypto)
                            }
                            user.cryptoList = userCryptoList
                            firestoreService.updateUser(user,null)
                        }
                        loadUserCryptos()
                        addRealtimeDataBaseListeners(user!!,cryptoList!!)

                    }
                    override fun onFailed(exception: Exception) {
                        showErrorMessage()
                    }
                })
                this@TraderActivity.runOnUiThread{
                    cryptoAdapter.cryptoList = cryptoList!!
                    cryptoAdapter.notifyDataSetChanged()

                }

            }

            override fun onFailed(exception: Exception) {
                Log.w("ERROR","ERROR ${exception.localizedMessage}")
                showErrorMessage()
            }

        })
    }

    private fun addRealtimeDataBaseListeners(user: User, cryptoList: List<Crypto>) {
            firestoreService.listenForUpdates(user,object:RealtimeDataListener<User>{
                override fun onDataChange(updatedData: User) {
                   this@TraderActivity.user = updatedData
                    loadUserCryptos()
                }

                override fun onError(exception: Exception) {
                    showErrorMessage()
                }

            })

        firestoreService.listenForUpdates(cryptoList,object:RealtimeDataListener<Crypto>{
            override fun onDataChange(updatedData: Crypto) {
                    var pos = 0
                for(crypto in cryptoAdapter.cryptoList){
                    if(crypto.name.equals(updatedData.name)){
                        crypto.available = updatedData.available
                        cryptoAdapter.notifyItemChanged(pos)
                    }
                    pos++
                }
            }

            override fun onError(exception: Exception) {
                showErrorMessage()
            }

        })
    }

    private fun loadUserCryptos() {
        runOnUiThread {
            if(user != null && user.cryptoList!= null){
                infoPanel.removeAllViews()
                for(crypto in user.cryptoList!!){
                    addUserCryptoInfoRow(crypto)
                }
            }
        }
    }

    private fun addUserCryptoInfoRow(crypto: Crypto) {
        val view = LayoutInflater.from(this).inflate(R.layout.coin_info,infoPanel,false)
        view.findViewById<TextView>(R.id.coinLabel).text = getString(R.string.coin_info,crypto.name,crypto.available.toString())
        Picasso.get().load(crypto.imageUrl).into(view.findViewById<ImageView>(R.id.coinIcon))
        infoPanel.addView(view)
    }

    private fun configureRecyclerView() {
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = cryptoAdapter
    }


    override fun onBuyCryptoClick(crypto: Crypto) {
                if(crypto.available > 0){
                        for(userCrypto in user.cryptoList!!){
                            if(userCrypto.name == crypto.name){
                                userCrypto.available+=1
                                break
                            }
                        }

                    crypto.available--
                    firestoreService.updateUser(user,null)
                    firestoreService.updateCrypto(crypto)
                }
    }



    private fun showErrorMessage() {
        Snackbar.make(fab, getString(R.string.error_while_connecting_to_the_server), Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }
}