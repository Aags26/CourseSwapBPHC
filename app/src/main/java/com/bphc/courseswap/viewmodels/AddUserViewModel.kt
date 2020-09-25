package com.bphc.courseswap.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bphc.courseswap.models.User
import com.bphc.courseswap.repositories.AddUserRepository

class AddUserViewModel @ViewModelInject constructor(
    private val repository: AddUserRepository
): ViewModel() {

    fun addUser(user: User): LiveData<Boolean>{
        return repository.addUser(user)
    }

}