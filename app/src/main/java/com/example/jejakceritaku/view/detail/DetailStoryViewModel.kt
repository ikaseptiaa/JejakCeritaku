package com.example.jejakceritaku.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.jejakceritaku.data.UserRepository
import com.example.jejakceritaku.data.pref.UserModel

class DetailStoryViewModel(private val repository: UserRepository) : ViewModel() {
    val detail = repository.storyDetail
    fun getDetailStory(token: String, id: String) = repository.getDetailStory(token, id)

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}