package com.bphc.courseswap.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bphc.courseswap.R
import com.bphc.courseswap.firebase.Auth
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_phone_auth.*
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PhoneAuthFragment : Fragment(), View.OnClickListener {


    private var verificationInProgress: Boolean? = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var inputMobile: String
    private lateinit var inputOTP: String

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
        return inflater.inflate(R.layout.fragment_phone_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_getOtp.setOnClickListener(this)
        resend_otp.setOnClickListener(this)

        if (savedInstanceState != null) {
            onViewStateRestored(savedInstanceState)
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                storedVerificationId = p0
                resendToken = p1
                updateUI(STATE_CODE_SENT)
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                verificationInProgress = false
                updateUI(STATE_VERIFY_SUCCESS, p0)
                signInWithPhoneAuthCredential(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                verificationInProgress = false
                if (p0 is FirebaseAuthInvalidCredentialsException) {
                    mobile_number.error = "Invalid phone number"
                } else if (p0 is FirebaseTooManyRequestsException) {
                    Snackbar.make(
                        view.findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                updateUI(STATE_VERIFY_FAILED)
            }

        }

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.

        //updateUI(Auth.auth()?.currentUser)

        // [START_EXCLUDE]
        /* if (verificationInProgress && validateMobileNumber()) {
             startPhoneNumberVerification(inputMobile)
         } */
        // [END_EXCLUDE]
    }



    /*private fun signOut() {
        auth.signOut()
        updateUI(STATE_INITIALIZED)
    }*/

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_getOtp -> {

                if (button_getOtp.text.toString().startsWith("Get")) {
                    if (!validateMobileNumber())
                        return
                    startPhoneNumberVerification(inputMobile)
                } else {
                    if (!validateOTP())
                        return
                    val code = layout_text_otp.editText?.text.toString().trim()
                    verifyPhoneNumberWithCode(storedVerificationId, code)
                }

            }
            R.id.resend_otp -> {
                resendVerificationCode(mobile_number.editText?.text.toString().trim(), resendToken)
            }
        }
    }

    private fun startPhoneNumberVerification(mobileNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91$mobileNumber",
            60,
            TimeUnit.SECONDS,
            requireActivity(),
            callbacks
        )

        verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            requireActivity(),
            callbacks,
            token)
    }

    private fun validateMobileNumber(): Boolean {
        inputMobile = mobile_number.editText?.text.toString().trim()
        return if (inputMobile.isEmpty()) {
            mobile_number.error = "* Please type your mobile number"
            false
        } else {
            mobile_number.error = null
            true
        }
    }

    private fun validateOTP(): Boolean {
        inputOTP = layout_text_otp.editText?.text.toString().trim()
        return if (inputOTP.isEmpty()) {
            layout_text_otp.error = "* Cannot be empty"
            false
        } else {
            layout_text_otp.error = null
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        verificationInProgress?.let { outState.putBoolean(KEY_VERIFY_IN_PROGRESS, it) }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        verificationInProgress =
            savedInstanceState?.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Auth.auth().signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    updateUI(STATE_SIGN_IN_SUCCESS, user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        layout_text_otp.error = "Invalid code."
                    }
                    updateUI(STATE_SIGN_IN_FAILED)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGN_IN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(
        uiState: Int,
        user: FirebaseUser? = Auth.auth().currentUser,
        cred: PhoneAuthCredential? = null
    ) {
        when (uiState) {
            STATE_INITIALIZED -> {
                layout_text_otp.visibility = View.INVISIBLE
                resend_otp.visibility = View.INVISIBLE
                button_getOtp.text = "Get otp"
            }
            STATE_CODE_SENT -> {
                layout_text_otp.visibility = View.VISIBLE
                resend_otp.visibility = View.VISIBLE
                button_getOtp.text = "Verify"
            }
            STATE_VERIFY_FAILED -> {
                Toast.makeText(context, "Verification failed, try again after the timeout", Toast.LENGTH_SHORT).show()
            }
            STATE_VERIFY_SUCCESS -> {
                if (cred != null) {
                    if (cred.smsCode != null) {
                        layout_text_otp.editText?.setText(cred.smsCode.toString())
                    }
                }
            }
            STATE_SIGN_IN_FAILED -> {
                Toast.makeText(context, "Failed to sign in", Toast.LENGTH_SHORT).show()
            }
            STATE_SIGN_IN_SUCCESS -> {
                Toast.makeText(context, "Signed In", Toast.LENGTH_SHORT).show()
                Auth.userPhoneNumber = user?.phoneNumber.toString()
                navController = view?.let { Navigation.findNavController(it) }
                navController!!.navigate(R.id.action_phoneAuthFragment_to_makeSwapRequest)
            }
        }
    }

    companion object {

        private const val TAG = "PhoneAuthActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGN_IN_FAILED = 5
        private const val STATE_SIGN_IN_SUCCESS = 6

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PhoneAuthFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}