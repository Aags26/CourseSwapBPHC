package com.bphc.courseswap.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bphc.courseswap.models.Course
import com.bphc.courseswap.models.User
import com.bphc.courseswap.repositories.MySwapRequestsRepository

class MySwapRequestsViewModel @ViewModelInject constructor(
    private val repository: MySwapRequestsRepository
) : ViewModel() {

    fun fetchMyRequests(
        user: User
    ): LiveData<ArrayList<Course>> {
        return repository.fetchMyRequests(user)
    }

    fun deleteRequest(
        course: Course,
        user: User
    ): LiveData<Boolean> {
        return repository.deleteRequest(course, user)
    }


}