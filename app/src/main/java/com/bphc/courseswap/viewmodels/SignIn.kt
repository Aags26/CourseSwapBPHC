package com.bphc.courseswap.viewmodels

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bphc.courseswap.R
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

    private lateinit var mContext: Activity

    private lateinit var googleSignInClient: GoogleSignInClient
    var user: MutableLiveData<FirebaseUser>? = MutableLiveData()


    fun getGoogleClient(context: Activity): GoogleSignInClient {

        mContext = context

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(mContext.getString(R.string.server_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(mContext, gso)
        return googleSignInClient
    }

    private fun getAuth(): FirebaseAuth {
        return Firebase.auth
    }

    fun checkUser() {
        setUser(getAuth().currentUser)
    }

    fun signOut() {
        getAuth().signOut()
        getGoogleClient(mContext).signOut().addOnCompleteListener(mContext) {
            setUser(null)
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        getAuth().signInWithCredential(credential)
            .addOnCompleteListener(mContext) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    setUser(getAuth().currentUser)
                } else {
                    setUser(null)
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

    private fun setUser(firebaseUser: FirebaseUser?){
        user?.value = firebaseUser
    }

}