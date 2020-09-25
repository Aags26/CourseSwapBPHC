package com.bphc.courseswap.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object Database {

    fun instance(): FirebaseFirestore {
        return Firebase.firestore
    }

}