package com.bphc.courseswap.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bphc.courseswap.R
import com.bphc.courseswap.firebase.Auth
import com.bphc.courseswap.viewmodels.SignIn
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_sign_in.*

class SignInFragment : Fragment(), View.OnClickListener {

    private val mSignInViewModel: SignIn by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private var navController: NavController? = null

    private lateinit var mLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        signInButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)

        mLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            mSignInViewModel.getUserAccount(it.data)
        }

    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(requireActivity())
        mSignInViewModel.setUser(account)
        updateUI()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.signInButton -> signIn()
            R.id.signOutButton -> signOut()
        }
    }

    private fun signIn() {
        mLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun signOut() {
        mSignInViewModel.firebaseSignOut()
        googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            mSignInViewModel.googleSignOut()
        }
    }

    private fun updateUI() {

        mSignInViewModel.user()?.observe(requireActivity(), { user ->
            if (user != null) {
                if (user.email?.isNotEmpty()!!) {
                    Auth.userEmail = user.email.toString()
                    Toast.makeText(context, user.email, Toast.LENGTH_SHORT).show()
                    navController = view?.let { Navigation.findNavController(it) }
                    navController!!.navigate(R.id.action_signInFragment_to_phoneAuthFragment)
                }
            } else {
                Toast.makeText(context, "null", Toast.LENGTH_SHORT).show()
            }
        })

    }

    companion object {

        private const val RC_SIGN_IN = 9001

    }
}