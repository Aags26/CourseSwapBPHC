package com.bphc.courseswap.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bphc.courseswap.R
import com.bphc.courseswap.firebase.Auth
import com.bphc.courseswap.viewmodels.SignIn
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_sign_in.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class SignInFragment : Fragment(), View.OnClickListener {

    private lateinit var mSignInViewModel: SignIn
    private lateinit var googleSignInClient: GoogleSignInClient
    var navController: NavController? = null

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSignInViewModel = ViewModelProvider(this).get(SignIn::class.java)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        signInButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)

    }

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.signInButton -> signIn()
            R.id.signOutButton -> signOut()
        }
    }

    private fun signIn() {
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (RC_SIGN_IN == requestCode)
            mSignInViewModel.getUserAccount(data)
    }

    private fun signOut() {
        mSignInViewModel.signOut()
        googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            mSignInViewModel.setUser()
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
                } else
                    signOut()
            } else {
                Toast.makeText(context, "null", Toast.LENGTH_SHORT).show()
            }
        })

    }

    companion object {

        private const val RC_SIGN_IN = 9001

        fun newInstance(param1: String, param2: String) =
            SignInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}