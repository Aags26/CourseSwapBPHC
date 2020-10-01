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
    private val _isDeleted = MutableLiveData<Boolean>()
    private val db = Database.instance()

    fun fetchMyRequests(user: User): LiveData<ArrayList<Course>>{

        val data: ArrayList<Course> = ArrayList()

        db.collection("users/${user.userEmail?.split('@')?.get(0)}/swap")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    data.addAll(it.result!!.toObjects(Course::class.java))
                    _requests.value = data
                }
            }
        return _requests
    }

    fun deleteRequest(course: Course, user: User): LiveData<Boolean> {

        db.document("users/${user.userEmail?.split('@')?.get(0)}/swap/${course.assignedCourse}")
            .delete()
            .addOnCompleteListener {
                _isDeleted.value = it.isSuccessful
            }
        return _isDeleted
    }

}