package com.example.jejakceritaku.viewmodel


import androidx.lifecycle.ViewModel
import com.example.jejakceritaku.data.UserRepository

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    fun register(name: String, email: String, password: String) = repository.register(name, email, password)

}
