package com.bphc.courseswap

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.ViewModelProvider
import com.bphc.courseswap.viewmodels.SignIn
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mSignInViewModel: SignIn
    //private lateinit var activityResultContract: ActivityResultContract<>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mSignInViewModel = ViewModelProvider(this).get(SignIn::class.java)

        mSignInViewModel.user?.observe(this, {
            updateUI(it)
        })

        signInButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)

    }

    override fun onStart() {
        super.onStart()
        mSignInViewModel.checkUser()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.signInButton -> signIn()
            R.id.signOutButton -> signOut()
        }
    }

    private fun signIn() {
        startActivityForResult(mSignInViewModel.getGoogleClient(this).signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        mSignInViewModel.signOut()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (RC_SIGN_IN == requestCode)
            mSignInViewModel.getUserAccount(data)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, user.email, Toast.LENGTH_SHORT).show()
            /*val intent = Intent(this, PhoneAuthActivity::class.java)
            startActivity(intent) */
        } else {
            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        private const val RC_SIGN_IN = 9001
    }


}