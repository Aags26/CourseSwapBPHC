package com.bphc.courseswap.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bphc.courseswap.firebase.Auth
import com.bphc.courseswap.firebase.Database
import com.bphc.courseswap.models.Course
import com.bphc.courseswap.models.User
import javax.inject.Inject

class MakeSwapRequestRepository @Inject constructor() {

    private val db = Database.instance()
    private var isPosted: MutableLiveData<Boolean> = MutableLiveData()

    fun addCourse(course: Course): LiveData<Boolean> {

            db.document("users/${Auth.userEmail}/swap/${course.assignedCourse}")
                .set(course)
                .addOnCompleteListener {
                    isPosted.value = it.isSuccessful
                }

        return isPosted
    }

}