package com.bphc.courseswap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_phone_auth.*
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var inputMobile: String
    private lateinit var inputOTP: String
    private lateinit var mobile: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        mobile = findViewById(R.id.mobile_number)

        button_getOtp.setOnClickListener(this)
        resend_otp.setOnClickListener(this)

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        auth = Firebase.auth

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
                    // Invalid request
                    mobile_number.error = "Invalid phone number"
                } else if (p0 is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT).show()
                }

                // Show a message and update the UI
                updateUI(STATE_VERIFY_FAILED)
            }

        }

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        //val currentUser = auth.currentUser
        //updateUI(currentUser)

        // [START_EXCLUDE]
       /* if (verificationInProgress && validateMobileNumber()) {
            startPhoneNumberVerification(inputMobile)
        } */
        // [END_EXCLUDE]
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    // [START_EXCLUDE]
                    updateUI(STATE_SIGN_IN_SUCCESS, user)
                    // [END_EXCLUDE]
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
                        layout_text_otp.error = "Invalid code."
                        // [END_EXCLUDE]
                    }
                    // [START_EXCLUDE silent]
                    // Update UI
                    updateUI(STATE_SIGN_IN_FAILED)
                    // [END_EXCLUDE]
                }
            }
    }

    /*private fun signOut() {
        auth.signOut()
        updateUI(STATE_INITIALIZED)
    }*/

    override fun onClick(v: View) {
        when (v.id) {
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
            this,
            callbacks
        )

        verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
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
            this,
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
        user: FirebaseUser? = auth.currentUser,
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
                Toast.makeText(this, "Verification failed, try again after the timeout", Toast.LENGTH_SHORT).show()
            }
            STATE_VERIFY_SUCCESS -> {
                if (cred != null) {
                    if (cred.smsCode != null) {
                        layout_text_otp.editText?.setText(cred.smsCode.toString())
                    }
                }
            }
            STATE_SIGN_IN_FAILED -> {
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
            }
            STATE_SIGN_IN_SUCCESS -> {
                Toast.makeText(this, "Signed In", Toast.LENGTH_SHORT).show()
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
    }
}