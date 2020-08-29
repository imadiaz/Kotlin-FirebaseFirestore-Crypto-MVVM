package com.firebase.firestoreapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.firebase.firestoreapp.R
import com.firebase.firestoreapp.model.User
import com.firebase.firestoreapp.network.Callback
import com.firebase.firestoreapp.network.FirestoreService
import com.firebase.firestoreapp.network.USERS_COLLECTION_NAME
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception


const val USERNAME_KEY = "username_key"
class MainActivity : AppCompatActivity() {

    private val auth: FirebaseAuth =  FirebaseAuth.getInstance()
    private lateinit var firestoreService: FirestoreService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())
    }

    fun onStartClicked(view:View) {
        val buttonLogin = findViewById<Button>(R.id.btnLogin)
        buttonLogin.isEnabled = false
        auth.signInAnonymously().addOnCompleteListener { task ->
            if(task.isSuccessful){
                val username = findViewById<EditText>(R.id.edtUsername).text.toString()
                firestoreService.findUserByID(username ,object: Callback<User> {
                    override fun onSuccess(result: User?) {
                       if(result == null){
                           saveUser(username,view)
                       }else{
                           startMainActivity(username)
                       }
                    }
                    override fun onFailed(exception: Exception) {
                            showErrorMessage(view)
                    }
                })
            }else{
                buttonLogin.isEnabled=true
                this.showErrorMessage(view)
            }
        }
    }

    private fun saveUser(username:String,view:View){
        val user = User(username)
        firestoreService.setDocument(user, USERS_COLLECTION_NAME,username,object:Callback<Void>{
            override fun onSuccess(result: Void?) {
                startMainActivity(username)
            }
            override fun onFailed(exception: Exception) {
                showErrorMessage(view)
            }
        })

    }
    private fun showErrorMessage(view:View) {
        Snackbar.make(view, getString(R.string.error_while_connecting_to_the_server), Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }

    private fun startMainActivity(username: String) {
        val intent = Intent(this, TraderActivity::class.java)
        intent.putExtra(USERNAME_KEY, username)
        startActivity(intent)
        finish()
    }
}