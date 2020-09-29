package com.bphc.courseswap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bphc.courseswap.app.Constants
import kotlinx.android.synthetic.main.activity_matched_user.*

class MatchedUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matched_user)

        val senderEmail: String? = intent.getStringExtra(Constants.USER_EMAIL)
        val senderPhone: String? = intent.getStringExtra(Constants.USER_PHONE)
        val senderAssigned: String? = intent.getStringExtra(Constants.USER_ASSIGNED)
        val senderDesired: String? = intent.getStringExtra(Constants.USER_DESIRED)

        sender_email.text = senderEmail
        sender_phone.text = senderPhone
        sender_assigned.text = senderAssigned
        sender_desired.text = senderDesired

    }
}