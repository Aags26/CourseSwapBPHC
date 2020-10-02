package com.bphc.courseswap.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bphc.courseswap.R
import com.bphc.courseswap.firebase.Auth
import com.bphc.courseswap.models.User
import com.bphc.courseswap.viewmodels.AddUserViewModel
import com.bphc.courseswap.viewmodels.PhoneAuthViewModel
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_phone_auth.*
import java.util.concurrent.TimeUnit


class PhoneAuthFragment : Fragment(), View.OnClickListener {


    private var verificationInProgress: Boolean? = false
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var inputMobile: String
    private lateinit var inputOTP: String

    private var navController: NavController? = null

    private lateinit var mAddUserViewModel: AddUserViewModel
    private lateinit var mPhoneAuthViewModel: PhoneAuthViewModel

    private lateinit var mUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_phone_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPhoneAuthViewModel =
            ViewModelProvider(requireActivity()).get(PhoneAuthViewModel::class.java)
        mAddUserViewModel = ViewModelProvider(requireActivity()).get(AddUserViewModel::class.java)

        button_getOtp.setOnClickListener(this)
        resend_otp.setOnClickListener(this)

        if (savedInstanceState != null) {
            onViewStateRestored(savedInstanceState)
        }


    }

    override fun onStart() {
        super.onStart()
        initUI()
        showErrors()
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
                    mPhoneAuthViewModel.verifyCode(layout_text_otp.editText?.text.toString().trim())
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
            mPhoneAuthViewModel.callbacks()
        )

        verificationInProgress = true
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
            mPhoneAuthViewModel.callbacks(),
            token
        )
    }

    private fun showErrors() {
        mPhoneAuthViewModel.validateInput().observe(requireActivity(), {
            when(it) {
                INVALID_PHONE_NUMBER -> {
                    mobile_number.error = it
                }
                INVALID_CODE -> {
                    layout_text_otp.error = it
                }

            }
        })
    }

    private fun initUI() {

        mPhoneAuthViewModel.status().observe(requireActivity(), {
            when (it) {
                STATE_CODE_SENT -> {
                    updateUI(it)
                }
                STATE_VERIFY_SUCCESS -> {
                    updateUI(it, mPhoneAuthViewModel.getCredential())
                }
                STATE_VERIFY_FAILED -> {
                    updateUI(it)
                }
                STATE_SIGN_IN_FAILED -> {
                    updateUI(it)
                }
                STATE_SIGN_IN_SUCCESS -> {
                    updateUI(it, mPhoneAuthViewModel.getUser())
                }

            }
        })

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
        user: FirebaseUser? = Firebase.auth.currentUser,
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
                Toast.makeText(
                    context,
                    "Verification failed, try again after the timeout",
                    Toast.LENGTH_SHORT
                ).show()
            }
            STATE_VERIFY_SUCCESS -> {
                if (cred != null) {
                    if (cred.smsCode != null) {
                        layout_text_otp.editText?.setText(cred.smsCode.toString())
                        button_getOtp.visibility = View.GONE
                    }
                }
            }
            STATE_SIGN_IN_FAILED -> {
                Toast.makeText(context, "Failed to sign in", Toast.LENGTH_SHORT).show()
            }
            STATE_SIGN_IN_SUCCESS -> {

                Auth.userPhoneNumber = user?.phoneNumber.toString()

                /*var token: String? = null

                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                    token = it.token
                }*/

                mUser = User(
                    Auth.userEmail,
                    Auth.userPhoneNumber,
                    FirebaseInstanceId.getInstance().token
                )
                mAddUserViewModel.addUser(mUser).observe(requireActivity(), { isRegistered ->
                    if (isRegistered != null) {
                        if (isRegistered) {
                            Toast.makeText(
                                context,
                                "Account Created / already registered",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController = view?.let { Navigation.findNavController(it) }
                            navController!!.navigate(R.id.action_phoneAuthFragment_to_makeSwapRequestFragment)
                        } else {
                            Toast.makeText(context, "Account not created", Toast.LENGTH_SHORT)
                                .show()
                            return@observe
                        }
                        return@observe
                    }
                })

            }
        }
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

    companion object {

        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
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