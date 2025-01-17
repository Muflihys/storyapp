package com.dicoding.storyapp.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.storyapp.network.api.ApiService
import com.dicoding.storyapp.network.response.AddStoryResponse
import com.dicoding.storyapp.network.response.ListStoryItem
import com.dicoding.storyapp.network.response.LoginResponse
import com.dicoding.storyapp.network.response.RegisterResponse
import com.dicoding.storyapp.network.response.StoriesResponse
import com.dicoding.storyapp.utils.AuthPreferences
import com.dicoding.storyapp.utils.Event
import com.dicoding.storyapp.view.home.StoryPagingSource
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.UnknownHostException


class StoryRepository private constructor(
    private val pref: AuthPreferences, private val apiService: ApiService
) {
    private val _registerResponse = MutableLiveData<RegisterResponse>()
    val registerResponse: LiveData<RegisterResponse> = _registerResponse

    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> = _loginResponse

    private val _uploadResponse = MutableLiveData<AddStoryResponse>()
    val uploadResponse: LiveData<AddStoryResponse> = _uploadResponse

    private val _list = MutableLiveData<StoriesResponse>()
    val list: LiveData<StoriesResponse> = _list

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastText = MutableLiveData<Event<String>>()
    val toastText: LiveData<Event<String>> = _toastText

    fun postRegister(name: String, email: String, password: String) {
        _isLoading.value = true
        val client = apiService.postRegister(name, email, password)

        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>, response: Response<RegisterResponse>
            ) {
                try {
                    _isLoading.value = false
                    if (response.isSuccessful && response.body() != null) {
                        _registerResponse.value = response.body()
                        _toastText.value = Event(response.body()?.message.toString())
                    } else {
                        val jsonObject = response.errorBody()?.string()?.let { JSONObject(it) }
                        val error = jsonObject?.getBoolean("error")
                        val message = jsonObject?.getString("message")
                        _registerResponse.value = RegisterResponse(error, message)
                        _toastText.value = Event(
                            "${response.message()} ${response.code()}, $message"
                        )
                        Log.e(
                            "postRegister",
                            "onResponse: ${response.message()}, ${response.code()} $message"
                        )
                    }
                } catch (e: JSONException) {
                    _toastText.value = Event(e.message.toString())
                    Log.e("JSONException", "onResponse: ${e.message.toString()}")
                } catch (e: Exception) {
                    _toastText.value = Event(e.message.toString())
                    Log.e("Exception", "onResponse: ${e.message.toString()}")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _isLoading.value = false
                when (t) {
                    is UnknownHostException -> {
                        _toastText.value = Event("No Internet Connection")
                        Log.e("UnknownHostException", "onFailure: ${t.message.toString()}")
                    }

                    else -> {
                        _toastText.value = Event(t.message.toString())
                        Log.e("postRegister", "onFailure: ${t.message.toString()}")
                    }
                }
            }
        })
    }

    fun postLogin(email: String, password: String) {
        _isLoading.value = true
        val client = apiService.postLogin(email, password)

        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>, response: Response<LoginResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _loginResponse.value = response.body()
                    _toastText.value = Event(response.body()?.message.toString())
                } else {
                    val jsonObject = response.errorBody()?.string()?.let { JSONObject(it) }
                    val error = jsonObject?.getBoolean("error")
                    val message = jsonObject?.getString("message")
                    _loginResponse.value = LoginResponse(error, message, null)
                    _toastText.value = Event(
                        "${response.message()} ${response.code()}, $message"
                    )
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                _toastText.value = Event(t.message.toString())
            }
        })
    }

    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(config = PagingConfig(
            pageSize = 5
        ), pagingSourceFactory = {
            StoryPagingSource(pref, apiService)
        }).liveData
    }

    fun uploadStory(token: String, file: MultipartBody.Part, description: RequestBody) {
        _isLoading.value = true
        val client = apiService.postStory(token, file, description)

        client.enqueue(object : Callback<AddStoryResponse> {
            override fun onResponse(
                call: Call<AddStoryResponse>, response: Response<AddStoryResponse>
            ) {
                try {
                    _isLoading.value = false
                    if (response.isSuccessful && response.body() != null) {
                        _uploadResponse.value = response.body()
                        _toastText.value = Event(response.body()?.message.toString())
                    } else {
                        val jsonObject = response.errorBody()?.string()?.let { JSONObject(it) }
                        val error = jsonObject?.getBoolean("error")
                        val message = jsonObject?.getString("message")
                        _uploadResponse.value = AddStoryResponse(error, message)
                        _toastText.value = Event(
                            "${response.message()} ${response.code()}, $message"
                        )
                        Log.e(
                            "uploadStory",
                            "onResponse: ${response.message()}, ${response.code()} $message"
                        )
                    }
                } catch (e: JSONException) {
                    _toastText.value = Event(e.message.toString())
                    Log.e("JSONException", "onResponse: ${e.message.toString()}")
                } catch (e: Exception) {
                    _toastText.value = Event(e.message.toString())
                    Log.e("Exception", "onResponse: ${e.message.toString()}")
                }
            }

            override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                _isLoading.value = false
                when (t) {
                    is UnknownHostException -> {
                        _toastText.value = Event("No Internet Connection")
                        Log.e("UnknownHostException", "onFailure: ${t.message.toString()}")
                    }

                    else -> {
                        _toastText.value = Event(t.message.toString())
                        Log.e("uploadStory", "onFailure: ${t.message.toString()}")
                    }
                }
            }
        })
    }

    fun getSession(): LiveData<AuthModel> {
        return pref.getSession().asLiveData()
    }

    suspend fun saveSession(session: AuthModel) {
        pref.saveSession(session)
    }

    suspend fun login() {
        pref.login()
    }

    suspend fun logout() {
        pref.logout()
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            preferences: AuthPreferences, apiService: ApiService
        ): StoryRepository = instance ?: synchronized(this) {
            instance ?: StoryRepository(preferences, apiService)
        }.also { instance = it }
    }
}