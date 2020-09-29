package com.bphc.courseswap.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bphc.courseswap.firebase.Database
import com.bphc.courseswap.models.User
import javax.inject.Inject

class AddUserRepository @Inject constructor() {

    private val db = Database.instance()
    private var isAdded: MutableLiveData<Boolean> = MutableLiveData()

    fun addUser(user: User): LiveData<Boolean> {

        db.document("users/${user.userEmail?.split('@')?.get(0)}")
            .set(user)
            .addOnCompleteListener {
                isAdded.value = if (it.isSuccessful) {
                    Log.d("TAG", "Saved")
                    true
                } else {
                    Log.w("TAG", it.exception)
                    false
                }
            }
        return isAdded

    }

}