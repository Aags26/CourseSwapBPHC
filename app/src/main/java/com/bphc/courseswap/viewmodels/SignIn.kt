package com.bphc.courseswap.viewmodels

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bphc.courseswap.R
import com.bphc.courseswap.firebase.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignIn : ViewModel() {

    private var user: MutableLiveData<FirebaseUser>? = MutableLiveData()

    fun signOut() {
        Auth.auth().signOut()
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Auth.auth().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user?.value = Auth.auth().currentUser
                } else {
                    user?.value = null
                }
            }
    }

    fun getUserAccount(data: Intent?) {

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {

        }
    }

    fun setUser() {
        user?.value = null
    }

    fun user(): MutableLiveData<FirebaseUser>? {
        user?.value = Auth.auth().currentUser
        return user
    }

}