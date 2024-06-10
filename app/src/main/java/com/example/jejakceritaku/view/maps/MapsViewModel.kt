package com.example.jejakceritaku.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.jejakceritaku.data.UserRepository
import com.example.jejakceritaku.data.pref.UserModel
import com.example.jejakceritaku.data.response.ListStoryItem

class MapsViewModel  (private val repository: UserRepository) : ViewModel(){
    val storyMaps = repository.storyMaps

    fun getMapsStory(token: String, callback: (Result<List<ListStoryItem>>) -> Unit) {
        repository.getMapsStory(token, callback)
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun setStoryMaps(storyList: List<ListStoryItem>) {
        repository.setStoryMaps(storyList)
    }



}