package com.example.jejakceritaku.view.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.jejakceritaku.data.UserRepository
import com.example.jejakceritaku.data.pref.UserModel
import java.io.File

class AddViewModel(private val repository: UserRepository): ViewModel() {

    fun uploadImage(token: String, file: File, description: String) = repository.addStory(token, file, description)

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}