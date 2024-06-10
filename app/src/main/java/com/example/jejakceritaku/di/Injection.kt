package com.example.jejakceritaku.di

import android.content.Context
import com.example.jejakceritaku.api.ApiConfig
import com.example.jejakceritaku.data.UserRepository
import com.example.jejakceritaku.data.pref.UserPreference
import com.example.jejakceritaku.data.pref.dataStore

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(apiService, pref)
    }
}