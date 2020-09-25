package com.bphc.courseswap.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bphc.courseswap.models.Course
import com.bphc.courseswap.models.User
import com.bphc.courseswap.repositories.MakeSwapRequestRepository

class MakeSwapRequestViewModel @ViewModelInject constructor (
    private val repository: MakeSwapRequestRepository
) : ViewModel() {

    fun addCourse(course: Course): LiveData<Boolean> {
        return repository.addCourse(course)
    }

}