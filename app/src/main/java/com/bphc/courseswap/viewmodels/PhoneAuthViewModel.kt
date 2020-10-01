package com.bphc.courseswap.viewmodels

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bphc.courseswap.firebase.Auth
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class PhoneAuthViewModel @ViewModelInject constructor() : ViewModel() {

    private var verificationInProgress: Boolean? = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var phoneAuthCredential: PhoneAuthCredential

    private var status: MutableLiveData<Int> = MutableLiveData()
    private var error: MutableLiveData<String> = MutableLiveData()
    private var user: FirebaseUser? = null
    private var exception: String = ""

    fun callbacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                storedVerificationId = p0
                resendToken = p1
                status.value = STATE_CODE_SENT
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                phoneAuthCredential = p0

                verificationInProgress = false
                status.value = STATE_VERIFY_SUCCESS
                signInWithPhoneAuthCredential(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                verificationInProgress = false
                if (p0 is FirebaseAuthInvalidCredentialsException) {
                    error.value = INVALID_PHONE_NUMBER
                } else if (p0 is FirebaseTooManyRequestsException) {
                    exception = "Quota exceeded."
                }
                status.value = STATE_VERIFY_FAILED
            }

        }
        return callbacks
    }

    fun verifyCode(code: String) {
        verifyPhoneNumberWithCode(storedVerificationId, code)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Auth.auth().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    user = task.result?.user
                    status.value = STATE_SIGN_IN_SUCCESS
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        error.value = INVALID_CODE
                    }
                }
            }
    }

    fun getCredential(): PhoneAuthCredential {
        return phoneAuthCredential
    }

    fun status(): LiveData<Int> {
        return status
    }

    fun validateInput(): LiveData<String> {
        return error
    }

    fun getUser(): FirebaseUser? {
        return user
    }

    companion object {

        private const val TAG = "PhoneAuthActivity"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGN_IN_FAILED = 5
        private const val STATE_SIGN_IN_SUCCESS = 6

        private const val INVALID_PHONE_NUMBER = "Invalid phone number"
        private const val INVALID_CODE = "Invalid code."
    }

}