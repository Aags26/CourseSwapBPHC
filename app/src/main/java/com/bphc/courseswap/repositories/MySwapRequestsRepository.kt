package com.bphc.courseswap.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bphc.courseswap.firebase.Database
import com.bphc.courseswap.models.Course
import com.bphc.courseswap.models.User
import javax.inject.Inject

class MySwapRequestsRepository @Inject constructor() {

    private val _requests = MutableLiveData<ArrayList<Course>>()
    private val db = Database.instance()

    fun fetchMyRequests(user: User): LiveData<ArrayList<Course>>{

        val data: ArrayList<Course> = ArrayList()

        db.collection("users/${user.userEmail}/swap")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (swapRequest in it.result!!) {
                        data.add(swapRequest.toObject(Course::class.java))
                    }
                    _requests.value = data
                    Log.d("ReQuEsTs", data.toString())
                }
            }
        return _requests
    }

}