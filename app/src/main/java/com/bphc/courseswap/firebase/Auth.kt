package com.bphc.courseswap.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object Auth {

    lateinit var userEmail: String
    lateinit var userPhoneNumber: String


    fun auth() : FirebaseAuth {
        return Firebase.auth
    }



}