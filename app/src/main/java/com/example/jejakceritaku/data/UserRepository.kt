package com.example.jejakceritaku.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.jejakceritaku.api.ApiService
import com.example.jejakceritaku.data.pref.UserModel
import com.example.jejakceritaku.data.pref.UserPreference
import com.example.jejakceritaku.data.response.AddResponse
import com.example.jejakceritaku.data.response.DetailStoryResponse
import com.example.jejakceritaku.data.response.ListStoryItem
import com.example.jejakceritaku.data.response.LoginResponse
import com.example.jejakceritaku.data.response.Story
import com.example.jejakceritaku.data.response.StoryResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import com.example.jejakceritaku.data.pref.Result
import com.example.jejakceritaku.data.response.RegisterResponse
import com.example.jejakceritaku.view.story.StoryPaging
import java.io.IOException

class UserRepository(private val apiService: ApiService, private val preference: UserPreference) {

    private val _storyList = MutableLiveData<List<ListStoryItem>>()
    val storyList: LiveData<List<ListStoryItem>> = _storyList

    private val _storyDetail = MutableLiveData<Story>()
    val storyDetail: LiveData<Story> = _storyDetail

    private val _storyMaps = MutableLiveData<List<ListStoryItem>>()
    val storyMaps: LiveData<List<ListStoryItem>> = _storyMaps

    fun register(name: String, email: String, password: String): LiveData<Result<String>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.register(name, email, password)
            emit(Result.Success(response.message ?: "Registration successful"))
        } catch (e: HttpException) {
            val errorMessage = if (e.code() == 400) {
                "Email has been entered"
            } else {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, RegisterResponse::class.java)
                errorBody.message ?: "Unknown error"
            }
            emit(Result.Error(errorMessage))
        } catch (e: Exception) {
            emit(Result.Error("Network error: ${e.message}"))
        }
    }

    fun login(email: String, password: String): LiveData<Result<String?>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)
            emit(Result.Success(response.loginResult?.token))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, LoginResponse::class.java)
            emit(Result.Error(errorResponse.message!!))
        }
    }

    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                StoryPaging(apiService,"Bearer $token")
            }
        ).liveData
    }

    fun getDetailStory(token: String, id: String) {
        val client = apiService.getDetailStory("Bearer $token", id)
        client.enqueue(object : Callback<DetailStoryResponse> {
            override fun onResponse(call: Call<DetailStoryResponse>, response: Response<DetailStoryResponse>) {
                handleResponse(response)
            }

            override fun onFailure(call: Call<DetailStoryResponse>, t: Throwable) {
                handleError(t)
            }
        })
    }

    private fun handleResponse(response: Response<DetailStoryResponse>) {
        if (response.isSuccessful) {
            _storyDetail.value = response.body()?.story!!
        } else {
            Log.e(TAG, "onFailure: ${response.message()}")
        }
    }

    private fun handleError(t: Throwable) {
        Log.e(TAG, "onFailure: ${t.message.toString()}")
    }



    fun addStory(token: String, imageFile: File, description: String) : LiveData<Result<AddResponse>> = liveData{
        emit(Result.Loading)
        val imageRequest = imageFile.asRequestBody("image/jpeg".toMediaType())
        val descRequest = description.toRequestBody("text/plain".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            imageRequest
        )

        try {
            val response = apiService.addStory("Bearer $token", multipartBody, descRequest)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, AddResponse::class.java)
            emit(Result.Error(errorResponse.message.toString()))
        }

    }

    fun getMapsStory(token: String, callback: (kotlin.Result<List<ListStoryItem>>) -> Unit) {
        val client = apiService.getMapsStory("Bearer $token")
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                if (response.isSuccessful) {
                    callback(kotlin.Result.success(response.body()?.listStory ?: emptyList()))
                } else {
                    callback(kotlin.Result.failure(IOException("Error getting stories: ${response.message()}")))
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                callback(kotlin.Result.failure(IOException("Error getting stories: ${t.message}")))
            }
        })
    }

    fun setStoryMaps(storyList: List<ListStoryItem>) {
        _storyMaps.value = storyList
    }

    fun getSession(): Flow<UserModel> {
        return preference.getSession()
    }

    suspend fun saveSession(user: UserModel) {
        preference.saveSession(user)
    }

    suspend fun logout() {
        preference.logout()
        instance = null
    }

    companion object {
        private const val TAG = "MainViewModel"
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(apiService: ApiService, pref: UserPreference) =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, pref)
            }.also { instance = it }
    }
}