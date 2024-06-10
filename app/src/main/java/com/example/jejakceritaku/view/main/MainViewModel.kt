package com.example.jejakceritaku.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.jejakceritaku.data.UserRepository
import com.example.jejakceritaku.data.pref.UserModel
import com.example.jejakceritaku.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MainViewModel (private val repository: UserRepository) : ViewModel() {

    val listStory = repository.storyList
    fun login(email: String, password: String) = repository.login(email, password)
    fun register(name: String, email: String, password: String) = repository.register(name, email, password)
    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> = repository.getStory(token).cachedIn(viewModelScope)


    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

}